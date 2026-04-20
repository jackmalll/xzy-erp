package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

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

}
