package cn.xzy.module.erp.service.purchase;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xzy.module.erp.controller.admin.purchase.vo.order.ErpPurchaseSyncOrderItemRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.order.ErpPurchaseSyncOrderRespVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.xzy.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.xzy.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.xzy.module.erp.controller.admin.purchase.vo.basepriceconfig.ErpBasePriceConfigVO;
import cn.xzy.module.erp.lingxing.client.LingxingApiClient;
import cn.xzy.module.erp.lingxing.config.LingxingProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购订单同步 Service
 * 对接领星 OpenAPI，增量拉取采购订单并幂等写入 erp_purchase_order / erp_purchase_order_item
 */
@Slf4j
@Service
public class ErpPurchaseOrderSyncService {

    private static final String PURCHASE_ORDER_PATH = "/erp/sc/routing/data/local_inventory/purchaseOrderList";
    private static final String PRODUCT_LIST_PATH = "/erp/sc/routing/data/local_inventory/productList";
    private static final DateTimeFormatter LX_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PAGE_SIZE = 500;

    @Resource
    private LingxingApiClient lingxingApiClient;
    @Resource
    private LingxingProperties lingxingProperties;
    @Resource
    private ErpPurchaseOrderMapper purchaseOrderMapper;
    @Resource
    private ErpPurchaseOrderItemMapper purchaseOrderItemMapper;
    @Resource
    private ErpBasePriceConfigService basePriceConfigService;

    /**
     * 增量同步采购订单
     * 拉取最近 syncDays 天按 create_time 变化的订单，分页循环直到无数据。
     * 若传入 startDate / endDate 则按传入日期同步，否则按配置的 syncDays 天数计算。
     * 只保存状态为已完成（status=9）的订单。
     *
     * @param startDate 开始日期（yyyy-MM-dd HH:mm:ss），可为 null
     * @param endDate   结束日期（yyyy-MM-dd HH:mm:ss），可为 null
     * @return 同步结果描述
     */
    public String syncPurchaseOrders(String startDate, String endDate) {
        if (endDate == null || endDate.isEmpty()) {
            endDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
        if (startDate == null || startDate.isEmpty()) {
            startDate = DateUtil.format(
                    DateUtil.offsetDay(new Date(), -lingxingProperties.getSyncDays()), "yyyy-MM-dd HH:mm:ss");
        }
        String searchFieldTime = "create_time";

        int offset = 0;
        int total = 0;
        int totalCompleted = 0;
        boolean firstPage = true;

        while (true) {
            Map<String, Object> body = new HashMap<>();
            body.put("start_date", startDate);
            body.put("end_date", endDate);
            body.put("search_field_time", searchFieldTime);
            body.put("offset", offset);
            body.put("length", PAGE_SIZE);

            String responseBody = lingxingApiClient.post(PURCHASE_ORDER_PATH, body);
            JSONObject resp = JSONUtil.parseObj(responseBody);

            Integer code = resp.getInt("code");
            if (code == null || code != 0) {
                String message = resp.getStr("message");
                if (message == null || message.isEmpty()) {
                    message = resp.getStr("msg");
                }
                throw new RuntimeException(String.format(
                        "[ErpPurchaseOrderSyncService] 接口返回错误：code=%s, message=%s, error_details=%s, searchCondition=%s, response=%s",
                        code, message, resp.get("error_details"), JSONUtil.toJsonStr(body), responseBody));
            }

            if (firstPage) {
                total = resp.getInt("total") != null ? resp.getInt("total") : 0;
                log.info("[ErpPurchaseOrderSyncService][syncPurchaseOrders] 该时间段采购订单总条数：{}，搜索条件：start_date={}, end_date={}, search_field_time={}",
                        total, startDate, endDate, searchFieldTime);
                firstPage = false;
            }

            JSONArray data = resp.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                break;
            }

            List<ErpPurchaseSyncOrderRespVO> orders = JSONUtil.toList(data, ErpPurchaseSyncOrderRespVO.class);

            // 批次级别：收集本页所有订单的全部 SKU，去重后一次性拉取图片
            List<String> batchSkuList = orders.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus() == 9
                            && (o.getStatusShipped() == null || o.getStatusShipped() != 1))
                    .filter(o -> o.getItemList() != null)
                    .flatMap(o -> o.getItemList().stream())
                    .map(ErpPurchaseSyncOrderItemRespVO::getSku)
                    .filter(sku -> sku != null && !sku.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            Map<String, String> batchPicUrlMap = fetchPicUrlMap(batchSkuList);

            // 1. 逐单 upsert（主单 + 子项），不在事务内计算降本
            List<String> completedOrderSnList = new ArrayList<>();
            for (ErpPurchaseSyncOrderRespVO vo : orders) {
                upsertOrder(vo, batchPicUrlMap);
                if (vo.getStatus() != null && vo.getStatus() == 9
                        && (vo.getStatusShipped() == null || vo.getStatusShipped() != 1)) {
                    totalCompleted++;
                    completedOrderSnList.add(vo.getOrderSn());
                }
            }

            // 2. 整页落库后，批量计算降本金额并回写（一条SQL搞定整页）
            if (!completedOrderSnList.isEmpty()) {
                batchUpdateCostReduction(completedOrderSnList);
            }

            if (data.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }

        log.info("[ErpPurchaseOrderSyncService][syncPurchaseOrders] 同步完成，共拉取 {} 条，其中已完成（status=9）{} 条，搜索条件：start_date={}, end_date={}, search_field_time={}",
                total, totalCompleted, startDate, endDate, searchFieldTime);
        return String.format("采购订单同步完成，共拉取 %d 条，其中已完成（status=9）%d 条，搜索条件：start_date=%s, end_date=%s, search_field_time=%s",
                total, totalCompleted, startDate, endDate, searchFieldTime);
    }

    /**
     * 无参版本，由默认定时任务调用，使用配置的 syncDays 天数
     */
    public String syncPurchaseOrders() {
        return syncPurchaseOrders(null, null);
    }

    /**
     * 幂等 upsert：以 order_sn 为唯一键，存在则更新，不存在则插入。
     * 只处理已完成（status=9）的订单。
     *
     * @param picUrlMap 批次级别的 sku->picUrl 映射，由调用方统一拉取后传入
     */
    @Transactional(rollbackFor = Exception.class)
    public void upsertOrder(ErpPurchaseSyncOrderRespVO vo, Map<String, String> picUrlMap) {
        if (vo.getStatus() == null || vo.getStatus() != 9
                || Integer.valueOf(1).equals(vo.getStatusShipped())) {
            return;
        }
        ErpPurchaseOrderDO existing = purchaseOrderMapper.selectByOrderSn(vo.getOrderSn());
        ErpPurchaseOrderDO orderDO = buildOrderDO(vo);

        if (existing == null) {
            purchaseOrderMapper.insert(orderDO);
        } else {
            orderDO.setId(existing.getId());
            purchaseOrderMapper.updateById(orderDO);
        }

        if (vo.getItemList() != null && !vo.getItemList().isEmpty()) {
            ErpBasePriceConfigVO config = basePriceConfigService.getConfig();
            LocalDateTime orderCreateTime = parseDateTime(vo.getCreateTime());
            String optRealname = vo.getOptRealname();
            for (ErpPurchaseSyncOrderItemRespVO itemVO : vo.getItemList()) {
                upsertItem(vo.getOrderSn(), itemVO, picUrlMap.get(itemVO.getSku()), config, optRealname, orderCreateTime);
            }
        }

    }

    /**
     * 批量回写降本金额：一条窗口函数 SQL 算完整页，再逐行 UPDATE
     */
    private void batchUpdateCostReduction(List<String> orderSnList) {
        List<Map<String, Object>> rows = purchaseOrderItemMapper.batchCalcCostReduction(orderSnList);
        for (Map<String, Object> row : rows) {
            String orderSn = (String) row.get("orderSn");
            Object val = row.get("costReduction");
            BigDecimal cr = val instanceof BigDecimal ? (BigDecimal) val : new BigDecimal(val.toString());
            ErpPurchaseOrderDO existing = purchaseOrderMapper.selectByOrderSn(orderSn);
            if (existing != null) {
                ErpPurchaseOrderDO update = new ErpPurchaseOrderDO();
                update.setId(existing.getId());
                update.setCostReduction(cr);
                purchaseOrderMapper.updateById(update);
            }
        }
    }

    private void upsertItem(String orderSn, ErpPurchaseSyncOrderItemRespVO itemVO, String picUrl,
                             ErpBasePriceConfigVO config, String optRealname, LocalDateTime orderCreateTime) {
        ErpPurchaseOrderItemDO existing = purchaseOrderItemMapper.selectByLxItemId(itemVO.getId());
        ErpPurchaseOrderItemDO itemDO = buildItemDO(orderSn, itemVO, picUrl, config, optRealname, orderCreateTime);
        if (existing == null) {
            purchaseOrderItemMapper.insert(itemDO);
        } else {
            itemDO.setId(existing.getId());
            purchaseOrderItemMapper.updateById(itemDO);
        }
    }

    private ErpPurchaseOrderDO buildOrderDO(ErpPurchaseSyncOrderRespVO vo) {
        // 完善点1：若入库量 != 实际采购量，则实际总价 = 各 item 的 quantity_entry * price 之和
        BigDecimal actualAmountTotal = vo.getAmountTotal();
        Integer qtyReal = vo.getQuantityReal();
        Integer qtyEntry = vo.getQuantityEntry();
        boolean partialEntry = qtyReal != null && qtyEntry != null && !qtyReal.equals(qtyEntry);
        if (partialEntry && vo.getItemList() != null && !vo.getItemList().isEmpty()) {
            BigDecimal sumEntry = BigDecimal.ZERO;
            for (ErpPurchaseSyncOrderItemRespVO item : vo.getItemList()) {
                if (item.getQuantityEntry() != null && item.getPrice() != null) {
                    sumEntry = sumEntry.add(
                            item.getPrice().multiply(BigDecimal.valueOf(item.getQuantityEntry())));
                }
            }
            actualAmountTotal = sumEntry;
        }

        ErpPurchaseOrderDO orderDO = ErpPurchaseOrderDO.builder()
                .orderSn(vo.getOrderSn())
                .optRealname(vo.getOptRealname())
                .status(vo.getStatus())
                .statusShipped(vo.getStatusShipped())
                .isTax(vo.getIsTax())
                .amountTotal(actualAmountTotal)
                .shippingPrice(vo.getShippingPrice())
                .quantityReal(qtyReal)
                .quantityEntry(qtyEntry)
                .lxCreateTime(parseDateTime(vo.getCreateTime()))
                .lxLastTime(parseDateTime(vo.getLastTime()))
                .build();
        orderDO.setCreator(vo.getOptRealname());
        return orderDO;
    }

    private ErpPurchaseOrderItemDO buildItemDO(String orderSn, ErpPurchaseSyncOrderItemRespVO itemVO, String picUrl,
                                               ErpBasePriceConfigVO config, String optRealname,
                                               LocalDateTime orderCreateTime) {
        // 完善点2：根据基准单价（N）配置规则计算 basePrice
        BigDecimal basePrice = calcBasePrice(itemVO.getSku(), orderSn, optRealname, orderCreateTime, config);

        // 完善点2：根据风控配置判断 itemStatus（1-有效 2-无效 0-待审核）
        Integer itemStatus = calcItemStatus(itemVO, basePrice, config);

        return ErpPurchaseOrderItemDO.builder()
                .orderSn(orderSn)
                .lxItemId(itemVO.getId())
                .productName(itemVO.getProductName())
                .sku(itemVO.getSku())
                .price(itemVO.getPrice())
                .amount(itemVO.getAmount())
                .quantityReal(itemVO.getQuantityReal())
                .quantityEntry(itemVO.getQuantityEntry())
                .picUrl(picUrl)
                .basePrice(basePrice)
                .itemStatus(itemStatus)
                .build();
    }

    /**
     * 根据基准单价（N）配置规则计算该 SKU 的基准单价。
     * 优先级：最小值规则 > 三段式规则。若未启用任何规则或无历史记录，则返回 null。
     */
    private BigDecimal calcBasePrice(String sku, String orderSn, String optRealname,
                                     LocalDateTime orderCreateTime, ErpBasePriceConfigVO config) {
        if (sku == null || optRealname == null || orderCreateTime == null) {
            return null;
        }
        List<BigDecimal> histPrices = purchaseOrderItemMapper
                .selectAllHistoryPricesBySku(sku, orderSn, optRealname, orderCreateTime);
        if (histPrices == null || histPrices.isEmpty()) {
            return null;
        }

        if (Boolean.TRUE.equals(config.getRuleMinPrice())) {
            return histPrices.stream().filter(p -> p != null)
                    .min(BigDecimal::compareTo).orElse(null);
        }

        if (Boolean.TRUE.equals(config.getRuleThreeEnabled())) {
            return calcThreeStageBasePrice(histPrices, config);
        }

        return null;
    }

    /**
     * 三段式规则计算基准单价：根据历史采购次数选中规则区间，对区间内价格取均值或最小值。
     */
    private BigDecimal calcThreeStageBasePrice(List<BigDecimal> prices, ErpBasePriceConfigVO config) {
        int n = prices.size();
        int fromIdx;
        int toIdx;

        Integer r1Max = config.getRule1MaxCount() != null ? config.getRule1MaxCount() : 3;
        Integer r2Max = config.getRule2MaxCount() != null ? config.getRule2MaxCount() : 10;

        if (n <= r1Max) {
            fromIdx = (config.getRule1AvgFrom() != null ? config.getRule1AvgFrom() : 1) - 1;
            toIdx   = Math.min(config.getRule1AvgTo() != null ? config.getRule1AvgTo() : 3, n) - 1;
        } else if (n <= r2Max) {
            fromIdx = (config.getRule2AvgFrom() != null ? config.getRule2AvgFrom() : 5) - 1;
            toIdx   = Math.min(config.getRule2AvgTo() != null ? config.getRule2AvgTo() : 8, n) - 1;
        } else {
            fromIdx = (config.getRule3AvgFrom() != null ? config.getRule3AvgFrom() : 5) - 1;
            Integer r3To = config.getRule3AvgTo();
            toIdx = (r3To == null || r3To == -1) ? n - 1 : Math.min(r3To, n) - 1;
        }

        if (fromIdx < 0) fromIdx = 0;
        if (toIdx < fromIdx || toIdx >= n) toIdx = n - 1;

        List<BigDecimal> slice = prices.subList(fromIdx, toIdx + 1).stream()
                .filter(p -> p != null).collect(Collectors.toList());
        if (slice.isEmpty()) return null;

        boolean useMin = "min".equalsIgnoreCase(config.getRuleThreeCalcType());
        if (useMin) {
            return slice.stream().min(BigDecimal::compareTo).orElse(null);
        }
        BigDecimal sum = slice.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(slice.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 完善点2：根据风控配置判断 item 的采购有效状态。
     * <ul>
     *   <li>1 - 有效（默认，未命中任何风控规则）</li>
     *   <li>2 - 无效/忽略（命中数量或金额风控中任意一条且均已启用）</li>
     *   <li>0 - 待审核（命中单价浮动风控）</li>
     * </ul>
     * 只有同时勾选（启用）的风控规则才参与判断。
     * 同一 item 命中无效规则优先于待审核。
     */
    private Integer calcItemStatus(ErpPurchaseSyncOrderItemRespVO itemVO, BigDecimal basePrice,
                                   ErpBasePriceConfigVO config) {
        boolean invalid = false;
        boolean pending = false;

        // 规则1：采购SKU数量 < 阈值 → 无效
        if (Boolean.TRUE.equals(config.getRiskQtyEnabled())
                && config.getRiskQtyThreshold() != null
                && itemVO.getQuantityReal() != null
                && itemVO.getQuantityReal() < config.getRiskQtyThreshold()) {
            invalid = true;
        }

        // 规则2：采购SKU总金额 < 阈值 → 无效（使用 amount 字段，即价税合计）
        if (Boolean.TRUE.equals(config.getRiskAmountEnabled())
                && config.getRiskAmountThreshold() != null
                && itemVO.getAmount() != null
                && itemVO.getAmount().compareTo(config.getRiskAmountThreshold()) < 0) {
            invalid = true;
        }

        // 规则3：本次单价浮动超过阈值 → 待审核（需有基准单价）
        if (!invalid && Boolean.TRUE.equals(config.getRiskPriceFloatEnabled())
                && config.getRiskPriceFloatPercent() != null
                && basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0
                && itemVO.getPrice() != null) {
            BigDecimal floatPct = itemVO.getPrice().subtract(basePrice)
                    .abs()
                    .divide(basePrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            if (floatPct.compareTo(config.getRiskPriceFloatPercent()) > 0) {
                pending = true;
            }
        }

        if (invalid) return 2;
        if (pending) return 0;
        return 1;
    }

    /**
     * 批量获取 SKU 对应的图片链接
     * 调用领星本地产品列表接口，返回 sku -> picUrl 映射。接口异常时不抛出，返回空 Map（不影响主流程）。
     */
    private Map<String, String> fetchPicUrlMap(List<String> skuList) {
        Map<String, String> result = new HashMap<>();
        if (skuList == null || skuList.isEmpty()) {
            return result;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("sku_list", skuList);
            String responseBody = lingxingApiClient.post(PRODUCT_LIST_PATH, body);
            JSONObject resp = JSONUtil.parseObj(responseBody);
            Integer code = resp.getInt("code");
            if (code == null || code != 0) {
                log.warn("[ErpPurchaseOrderSyncService][fetchPicUrlMap] 接口返回错误：code={}, message={}",
                        code, resp.getStr("message"));
                return result;
            }
            JSONArray data = resp.getJSONArray("data");
            if (data != null) {
                data.forEach(item -> {
                    JSONObject obj = (JSONObject) item;
                    String sku = obj.getStr("sku");
                    String picUrl = obj.getStr("pic_url");
                    if (sku != null && picUrl != null) {
                        result.put(sku, picUrl);
                    }
                });
            }
        } catch (Exception e) {
            log.warn("[ErpPurchaseOrderSyncService][fetchPicUrlMap] 获取图片链接失败，skuList={}", skuList, e);
        }
        return result;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, LX_DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("[ErpPurchaseOrderSyncService] 日期解析失败：{}", dateStr);
            return null;
        }
    }

}
