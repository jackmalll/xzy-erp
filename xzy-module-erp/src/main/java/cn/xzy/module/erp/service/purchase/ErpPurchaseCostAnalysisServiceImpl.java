package cn.xzy.module.erp.service.purchase;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.common.util.object.BeanUtils;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisPageReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.xzy.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.xzy.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
        PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderMapper.selectCostAnalysisPage(pageReqVO);
        List<ErpPurchaseCostAnalysisRespVO> voList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .toList();
        return new PageResult<>(voList, pageResult.getTotal());
    }

    private ErpPurchaseCostAnalysisRespVO convertToRespVO(ErpPurchaseOrderDO order) {
        ErpPurchaseCostAnalysisRespVO vo = BeanUtils.toBean(order, ErpPurchaseCostAnalysisRespVO.class);

        // 采购实付金额 = 货物总价 + 运费
        BigDecimal payment = order.getAmountTotal() != null ? order.getAmountTotal() : BigDecimal.ZERO;
        BigDecimal shippingPrice = order.getShippingPrice() != null ? order.getShippingPrice() : BigDecimal.ZERO;
        BigDecimal actualPayment = payment.add(shippingPrice);
        vo.setActualPayment(actualPayment);

        // 查询子项列表，统计 SKU 种类数
        List<ErpPurchaseOrderItemDO> items = purchaseOrderItemMapper.selectListByOrderSn(order.getOrderSn());
        vo.setSkuCount(items.size());

        // 降本金额 = 所有SKU的降本金额之和
        // 每个SKU降本金额 = (第1次历史采购单价 - 本次采购单价) × 本次采购数量（无历史价则跳过）
        BigDecimal totalCostReduction = BigDecimal.ZERO;
        for (ErpPurchaseOrderItemDO item : items) {
            List<ErpPurchaseOrderItemDetailVO.HistoryPrice> historyPrices =
                    purchaseOrderItemMapper.selectHistoryPricesBySku(item.getSku(), order.getOrderSn(), 1);
            if (historyPrices.isEmpty() || historyPrices.get(0).getPrice() == null) {
                continue;
            }
            BigDecimal historyPrice = historyPrices.get(0).getPrice();
            BigDecimal currentPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            int qty = item.getQuantityReal() != null ? item.getQuantityReal() : 0;
            BigDecimal skuReduction = historyPrice.subtract(currentPrice).multiply(BigDecimal.valueOf(qty));
            totalCostReduction = totalCostReduction.add(skuReduction);
        }
        vo.setCostReduction(totalCostReduction);

        return vo;
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
            vo.setOptRealname(order != null ? order.getOptRealname() : null);
            vo.setPrice(item.getPrice());
            vo.setPurchaseDate(order != null ? order.getLxCreateTime() : null);
            vo.setPicUrl(item.getPicUrl());

            List<ErpPurchaseOrderItemDetailVO.HistoryPrice> historyPrices =
                    purchaseOrderItemMapper.selectHistoryPricesBySku(item.getSku(), orderSn, 3);
            vo.setHistoryPrices(historyPrices);
            return vo;
        }).toList();
    }

}
