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
import cn.xzy.module.erp.lingxing.client.LingxingApiClient;
import cn.xzy.module.erp.lingxing.config.LingxingProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购订单同步 Service
 * 对接领星 OpenAPI，增量拉取采购订单并幂等写入 erp_purchase_order / erp_purchase_order_item
 */
@Slf4j
@Service
public class ErpPurchaseOrderSyncService {

    private static final String PURCHASE_ORDER_PATH = "/erp/sc/routing/data/local_inventory/purchaseOrderList";
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

    /**
     * 增量同步采购订单
     * 拉取最近 syncDays 天按 update_time 变化的订单，分页循环直到无数据
     *
     * @return 同步结果描述
     */
    public String syncPurchaseOrders() {
        String endDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String startDate = DateUtil.format(
                DateUtil.offsetDay(new Date(), -lingxingProperties.getSyncDays()), "yyyy-MM-dd HH:mm:ss");
        String searchFieldTime = "create_time";

        int offset = 0;
        int totalSynced = 0;

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

            JSONArray data = resp.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                break;
            }

            List<ErpPurchaseSyncOrderRespVO> orders = JSONUtil.toList(data, ErpPurchaseSyncOrderRespVO.class);
            for (ErpPurchaseSyncOrderRespVO vo : orders) {
                upsertOrder(vo);
                totalSynced++;
            }

            if (data.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }

        log.info("[ErpPurchaseOrderSyncService][syncPurchaseOrders] 同步完成，共同步 {} 条，搜索条件：start_date={}, end_date={}, search_field_time={}",
                totalSynced, startDate, endDate, searchFieldTime);
        return String.format("采购订单同步完成，共同步 %d 条，搜索条件：start_date=%s, end_date=%s, search_field_time=%s",
                totalSynced, startDate, endDate, searchFieldTime);
    }

    /**
     * 幂等 upsert：以 order_sn 为唯一键，存在则更新，不存在则插入
     */
    @Transactional(rollbackFor = Exception.class)
    public void upsertOrder(ErpPurchaseSyncOrderRespVO vo) {
        ErpPurchaseOrderDO existing = purchaseOrderMapper.selectByOrderSn(vo.getOrderSn());
        ErpPurchaseOrderDO orderDO = buildOrderDO(vo);

        if (existing == null) {
            purchaseOrderMapper.insert(orderDO);
        } else {
            orderDO.setId(existing.getId());
            purchaseOrderMapper.updateById(orderDO);
        }

        if (vo.getItemList() != null) {
            for (ErpPurchaseSyncOrderItemRespVO itemVO : vo.getItemList()) {
                upsertItem(vo.getOrderSn(), itemVO);
            }
        }
    }

    private void upsertItem(String orderSn, ErpPurchaseSyncOrderItemRespVO itemVO) {
        ErpPurchaseOrderItemDO existing = purchaseOrderItemMapper.selectByLxItemId(itemVO.getId());
        ErpPurchaseOrderItemDO itemDO = buildItemDO(orderSn, itemVO);
        if (existing == null) {
            purchaseOrderItemMapper.insert(itemDO);
        } else {
            itemDO.setId(existing.getId());
            purchaseOrderItemMapper.updateById(itemDO);
        }
    }

    private ErpPurchaseOrderDO buildOrderDO(ErpPurchaseSyncOrderRespVO vo) {
        return ErpPurchaseOrderDO.builder()
                .orderSn(vo.getOrderSn())
                .optRealname(vo.getOptRealname())
                .status(vo.getStatus())
                .statusShipped(vo.getStatusShipped())
                .isTax(vo.getIsTax())
                .payment(vo.getPayment())
                .shippingPrice(vo.getShippingPrice())
                .lxCreateTime(parseDateTime(vo.getCreateTime()))
                .lxLastTime(parseDateTime(vo.getLastTime()))
                .build();
    }

    private ErpPurchaseOrderItemDO buildItemDO(String orderSn, ErpPurchaseSyncOrderItemRespVO itemVO) {
        return ErpPurchaseOrderItemDO.builder()
                .orderSn(orderSn)
                .lxItemId(itemVO.getId())
                .productName(itemVO.getProductName())
                .sku(itemVO.getSku())
                .price(itemVO.getPrice())
                .amount(itemVO.getAmount())
                .quantityReal(itemVO.getQuantityReal())
                .build();
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
