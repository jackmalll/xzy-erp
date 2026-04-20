package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购订单 Mapper
 */
@Mapper
public interface ErpPurchaseOrderMapper extends BaseMapperX<ErpPurchaseOrderDO> {

    /**
     * 根据领星采购单号查询
     */
    default ErpPurchaseOrderDO selectByOrderSn(String orderSn) {
        return selectOne(ErpPurchaseOrderDO::getOrderSn, orderSn);
    }

}
