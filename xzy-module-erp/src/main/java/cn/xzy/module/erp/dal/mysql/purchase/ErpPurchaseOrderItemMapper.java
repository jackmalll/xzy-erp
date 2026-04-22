package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 查询某 SKU 在排除指定订单号之外的历史采购记录（按采购创建时间倒序，限前 limit 条）
     */
    List<ErpPurchaseOrderItemDetailVO.HistoryPrice> selectHistoryPricesBySku(
            @Param("sku") String sku,
            @Param("excludeOrderSn") String excludeOrderSn,
            @Param("limit") int limit);

    /**
     * 批量查询多个订单的子项列表
     */
    default List<ErpPurchaseOrderItemDO> selectListByOrderSnIn(Collection<String> orderSnList) {
        return selectList(ErpPurchaseOrderItemDO::getOrderSn, orderSnList);
    }

    /**
     * 批量计算指定订单列表的降本金额。
     * 逻辑：对每个订单的每个 SKU，取该 SKU 在本订单之外最近一次采购单价作为历史价，
     * 降本金额 = SUM((历史单价 - 本次单价) × 本次采购数量)，无历史价的 SKU 不参与计算。
     * 返回：Map key=orderSn, value=costReduction
     */
    List<Map<String, Object>> selectCostReductionByOrderSnList(@Param("orderSnList") Collection<String> orderSnList);

}
