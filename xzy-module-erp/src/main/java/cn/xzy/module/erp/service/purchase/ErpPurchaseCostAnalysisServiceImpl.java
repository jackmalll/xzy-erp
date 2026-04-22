package cn.xzy.module.erp.service.purchase;

import cn.xzy.framework.common.pojo.PageParam;
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
        boolean hasCostFilter = pageReqVO.getCostReductionMin() != null || pageReqVO.getCostReductionMax() != null;

        if (!hasCostFilter) {
            // 无降本金额过滤：正常分页，只查当页数据（2次批量SQL）
            PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderMapper.selectCostAnalysisPage(pageReqVO);
            List<ErpPurchaseCostAnalysisRespVO> voList = convertBatch(pageResult.getList());
            return new PageResult<>(voList, pageResult.getTotal());
        }

        // 有降本金额过滤：全量拉取（仍是2次批量SQL），内存过滤后再手动分页
        ErpPurchaseCostAnalysisPageReqVO allReqVO = new ErpPurchaseCostAnalysisPageReqVO();
        allReqVO.setPageNo(1);
        allReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        allReqVO.setOrderSn(pageReqVO.getOrderSn());
        allReqVO.setOptRealname(pageReqVO.getOptRealname());
        allReqVO.setLxCreateTime(pageReqVO.getLxCreateTime());
        PageResult<ErpPurchaseOrderDO> allResult = purchaseOrderMapper.selectCostAnalysisPage(allReqVO);
        List<ErpPurchaseCostAnalysisRespVO> allVoList = convertBatch(allResult.getList()).stream()
                .filter(vo -> {
                    BigDecimal cr = vo.getCostReduction() != null ? vo.getCostReduction() : BigDecimal.ZERO;
                    if (pageReqVO.getCostReductionMin() != null && cr.compareTo(pageReqVO.getCostReductionMin()) < 0) {
                        return false;
                    }
                    if (pageReqVO.getCostReductionMax() != null && cr.compareTo(pageReqVO.getCostReductionMax()) > 0) {
                        return false;
                    }
                    return true;
                })
                .toList();
        long total = allVoList.size();
        int fromIndex = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
        int toIndex = Math.min(fromIndex + pageReqVO.getPageSize(), (int) total);
        List<ErpPurchaseCostAnalysisRespVO> pagedList = fromIndex >= total
                ? Collections.emptyList()
                : allVoList.subList(fromIndex, toIndex);
        return new PageResult<>(pagedList, total);
    }

    /**
     * 批量将订单 DO 转换为 RespVO，仅触发 2 次 SQL：
     * 1. 批量查全部子项（IN 查询）
     * 2. 批量计算降本金额（窗口函数聚合）
     */
    private List<ErpPurchaseCostAnalysisRespVO> convertBatch(List<ErpPurchaseOrderDO> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> orderSnList = orders.stream().map(ErpPurchaseOrderDO::getOrderSn).toList();

        // 1. 批量查子项，按 orderSn 分组，用于统计 SKU 种类数
        List<ErpPurchaseOrderItemDO> allItems = purchaseOrderItemMapper.selectListByOrderSnIn(orderSnList);
        Map<String, Long> skuCountMap = allItems.stream()
                .collect(Collectors.groupingBy(ErpPurchaseOrderItemDO::getOrderSn, Collectors.counting()));

        // 2. 批量用 SQL 窗口函数计算降本金额（一条 SQL 搞定所有订单）
        List<Map<String, Object>> costRows = purchaseOrderItemMapper.selectCostReductionByOrderSnList(orderSnList);
        Map<String, BigDecimal> costReductionMap = costRows.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("orderSn"),
                        row -> row.get("costReduction") instanceof BigDecimal
                                ? (BigDecimal) row.get("costReduction")
                                : new BigDecimal(row.get("costReduction").toString())
                ));

        return orders.stream().map(order -> {
            ErpPurchaseCostAnalysisRespVO vo = BeanUtils.toBean(order, ErpPurchaseCostAnalysisRespVO.class);
            BigDecimal payment = order.getAmountTotal() != null ? order.getAmountTotal() : BigDecimal.ZERO;
            BigDecimal shippingPrice = order.getShippingPrice() != null ? order.getShippingPrice() : BigDecimal.ZERO;
            vo.setActualPayment(payment.add(shippingPrice));
            vo.setSkuCount(skuCountMap.getOrDefault(order.getOrderSn(), 0L).intValue());
            vo.setCostReduction(costReductionMap.getOrDefault(order.getOrderSn(), BigDecimal.ZERO));
            return vo;
        }).toList();
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
