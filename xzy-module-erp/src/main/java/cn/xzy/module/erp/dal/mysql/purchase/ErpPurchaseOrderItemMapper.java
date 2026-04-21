package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    @Select("SELECT i.price, o.lx_create_time AS purchaseDate " +
            "FROM erp_purchase_order_item i " +
            "JOIN erp_purchase_order o ON o.order_sn = i.order_sn " +
            "WHERE i.sku = #{sku} AND i.order_sn != #{excludeOrderSn} " +
            "ORDER BY o.lx_create_time DESC " +
            "LIMIT #{limit}")
    List<ErpPurchaseOrderItemDetailVO.HistoryPrice> selectHistoryPricesBySku(
            @Param("sku") String sku,
            @Param("excludeOrderSn") String excludeOrderSn,
            @Param("limit") int limit);

}
