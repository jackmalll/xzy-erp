package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 采购订单子项 Mapper
 */
@Mapper
public interface ErpPurchaseOrderItemMapper extends BaseMapperX<ErpPurchaseOrderItemDO> {

    /**
     * 根据领星子项 id 查询
     */
    default ErpPurchaseOrderItemDO selectByLxItemId(Long lxItemId) {
        return selectOne(ErpPurchaseOrderItemDO::getLxItemId, lxItemId);
    }

    /**
     * 根据采购单号查询子项列表
     */
    default List<ErpPurchaseOrderItemDO> selectListByOrderSn(String orderSn) {
        return selectList(ErpPurchaseOrderItemDO::getOrderSn, orderSn);
    }

    /**
     * 查询某 SKU 在排除指定订单号之外、同一采购员、且采购日期早于本次的历史采购记录（按采购创建时间正序，最早的为第1次，限前 limit 条）
     */
    List<ErpPurchaseOrderItemDetailVO.HistoryPrice> selectHistoryPricesBySku(
            @Param("sku") String sku,
            @Param("excludeOrderSn") String excludeOrderSn,
            @Param("optRealname") String optRealname,
            @Param("purchaseDateBefore") LocalDateTime purchaseDateBefore,
            @Param("limit") int limit);

    /**
     * 批量查询多个订单的子项列表
     */
    default List<ErpPurchaseOrderItemDO> selectListByOrderSnIn(Collection<String> orderSnList) {
        return selectList(ErpPurchaseOrderItemDO::getOrderSn, orderSnList);
    }

    /**
     * 批量计算多个订单的降本金额（一条 SQL，供同步时整页调用）。
     * 返回 List<Map>，key=orderSn, value=costReduction
     */
    List<Map<String, Object>> batchCalcCostReduction(@Param("orderSnList") Collection<String> orderSnList);

    /**
     * 计算单个订单的降本金额。
     * 逻辑：每个 SKU 取本订单之外最近一次采购单价作为历史价，
     * 降本金额 = SUM((历史单价 - 本次单价) × 本次采购数量)，无历史价的 SKU 不参与计算。
     */
    BigDecimal calcCostReductionByOrderSn(@Param("orderSn") String orderSn);

    /**
     * 查询某 SKU 在排除指定订单号之外、同一采购员、且采购日期早于本次的最早一次采购单价（基准单价）。
     * 无历史记录时返回 null。
     */
    BigDecimal selectFirstHistoryPriceBySku(
            @Param("sku") String sku,
            @Param("excludeOrderSn") String excludeOrderSn,
            @Param("optRealname") String optRealname,
            @Param("purchaseDateBefore") LocalDateTime purchaseDateBefore);

}
