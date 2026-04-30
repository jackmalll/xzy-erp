package cn.xzy.module.erp.service.purchase;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.common.util.object.BeanUtils;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisPageReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostReportReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostReportRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.xzy.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.xzy.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购成本统计分析 Service 实现
 */
@Service
public class ErpPurchaseCostAnalysisServiceImpl implements ErpPurchaseCostAnalysisService {

    @Resource
    private ErpPurchaseOrderMapper purchaseOrderMapper;

    @Resource
    private ErpPurchaseOrderItemMapper purchaseOrderItemMapper;

    @Override
    public PageResult<ErpPurchaseCostAnalysisRespVO> getPurchaseCostAnalysisPage(ErpPurchaseCostAnalysisPageReqVO pageReqVO) {
        // cost_reduction 已持久化，DB 直接过滤分页，无需内存全量拉取
        PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderMapper.selectCostAnalysisPage(pageReqVO);
        List<ErpPurchaseCostAnalysisRespVO> voList = convertBatch(pageResult.getList());
        return new PageResult<>(voList, pageResult.getTotal());
    }

    /**
     * 批量将订单 DO 转换为 RespVO，触发 1 次批量 SQL 查子项统计 SKU 数，
     * 降本金额直接读 DO 持久化字段。
     */
    private List<ErpPurchaseCostAnalysisRespVO> convertBatch(List<ErpPurchaseOrderDO> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> orderSnList = orders.stream().map(ErpPurchaseOrderDO::getOrderSn).toList();

        // 批量查子项，按 orderSn 分组，用于统计 SKU 种类数
        List<ErpPurchaseOrderItemDO> allItems = purchaseOrderItemMapper.selectListByOrderSnIn(orderSnList);
        Map<String, Long> skuCountMap = allItems.stream()
                .collect(Collectors.groupingBy(ErpPurchaseOrderItemDO::getOrderSn, Collectors.counting()));

        return orders.stream().map(order -> {
            ErpPurchaseCostAnalysisRespVO vo = BeanUtils.toBean(order, ErpPurchaseCostAnalysisRespVO.class);
            BigDecimal payment = order.getAmountTotal() != null ? order.getAmountTotal() : BigDecimal.ZERO;
            BigDecimal shipping = order.getShippingPrice() != null ? order.getShippingPrice() : BigDecimal.ZERO;
            vo.setActualPayment(payment.add(shipping));
            vo.setSkuCount(skuCountMap.getOrDefault(order.getOrderSn(), 0L).intValue());
            vo.setCostReduction(order.getCostReduction());
            return vo;
        }).toList();
    }

    @Override
    public ErpPurchaseCostReportRespVO getPurchaseCostReport(ErpPurchaseCostReportReqVO reqVO) {
        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length >= 2) {
            beginTime = reqVO.getCreateTime()[0];
            endTime = reqVO.getCreateTime()[1];
        }
        List<ErpPurchaseCostReportRespVO.Item> items =
                purchaseOrderMapper.selectCostReportGroupByBuyer(beginTime, endTime);

        BigDecimal totalGoods = items.stream()
                .map(item -> item.getTotalAmountGoods() != null ? item.getTotalAmountGoods() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReduction = items.stream()
                .map(item -> item.getTotalCostReduction() != null ? item.getTotalCostReduction() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (ErpPurchaseCostReportRespVO.Item item : items) {
            BigDecimal goods = item.getTotalAmountGoods() != null ? item.getTotalAmountGoods() : BigDecimal.ZERO;
            BigDecimal reduction = item.getTotalCostReduction() != null ? item.getTotalCostReduction() : BigDecimal.ZERO;
            if (totalGoods.compareTo(BigDecimal.ZERO) > 0) {
                item.setAmountGoodsRatio(goods.multiply(BigDecimal.valueOf(100))
                        .divide(totalGoods, 0, RoundingMode.HALF_UP));
            } else {
                item.setAmountGoodsRatio(BigDecimal.ZERO);
            }
            if (totalReduction.compareTo(BigDecimal.ZERO) > 0) {
                item.setCostReductionRatio(reduction.multiply(BigDecimal.valueOf(100))
                        .divide(totalReduction, 0, RoundingMode.HALF_UP));
            } else {
                item.setCostReductionRatio(BigDecimal.ZERO);
            }
        }

        ErpPurchaseCostReportRespVO respVO = new ErpPurchaseCostReportRespVO();
        respVO.setItems(items);
        respVO.setTotalAmountGoodsAll(totalGoods);
        respVO.setTotalCostReductionAll(totalReduction);
        respVO.setStatBegin(beginTime);
        respVO.setStatEnd(endTime);
        return respVO;
    }

    @Override
    public int recalcAllCostReduction() {
        List<ErpPurchaseOrderDO> all = purchaseOrderMapper.selectList(null);
        for (ErpPurchaseOrderDO order : all) {
            BigDecimal cr = purchaseOrderItemMapper.calcCostReductionByOrderSn(order.getOrderSn());
            ErpPurchaseOrderDO update = new ErpPurchaseOrderDO();
            update.setId(order.getId());
            update.setCostReduction(cr);
            purchaseOrderMapper.updateById(update);
        }
        return all.size();
    }

    @Override
    public List<ErpPurchaseOrderItemDetailVO> getOrderItemDetails(String orderSn) {
        ErpPurchaseOrderDO order = purchaseOrderMapper.selectByOrderSn(orderSn);
        List<ErpPurchaseOrderItemDO> items = purchaseOrderItemMapper.selectListByOrderSn(orderSn);

        return items.stream().map(item -> {
            ErpPurchaseOrderItemDetailVO vo = new ErpPurchaseOrderItemDetailVO();
            vo.setProductName(item.getProductName());
            vo.setSku(item.getSku());
            vo.setQuantityReal(item.getQuantityReal());
            vo.setQuantityEntry(item.getQuantityEntry());
            vo.setOptRealname(order != null ? order.getOptRealname() : null);
            vo.setPrice(item.getPrice());
            vo.setPurchaseDate(order != null ? order.getLxCreateTime() : null);
            vo.setPicUrl(item.getPicUrl());
            vo.setItemStatus(item.getItemStatus());

            String optRealname = order != null ? order.getOptRealname() : null;
            java.time.LocalDateTime purchaseDate = order != null ? order.getLxCreateTime() : null;

            List<ErpPurchaseOrderItemDetailVO.HistoryPrice> historyPrices =
                    purchaseOrderItemMapper.selectHistoryPricesBySku(item.getSku(), orderSn, optRealname, purchaseDate, 3);
            vo.setHistoryPrices(historyPrices);

            BigDecimal basePrice = purchaseOrderItemMapper.selectFirstHistoryPriceBySku(item.getSku(), orderSn, optRealname, purchaseDate);
            vo.setBasePrice(basePrice);
            // 降本金额按入库数量计算：有入库数量取入库数量，否则降级使用采购数量
            // itemStatus=2（无效/忽略）时不参与降本计算
            Integer calcQty = item.getQuantityEntry() != null ? item.getQuantityEntry() : item.getQuantityReal();
            if (basePrice != null && item.getPrice() != null && calcQty != null
                    && !Integer.valueOf(2).equals(item.getItemStatus())) {
                vo.setItemCostReduction(basePrice.subtract(item.getPrice())
                        .multiply(BigDecimal.valueOf(calcQty)));
            }
            return vo;
        }).toList();
    }

}
