package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisPageReqVO;
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

    /**
     * 采购成本统计分析分页查询
     */
    default PageResult<ErpPurchaseOrderDO> selectCostAnalysisPage(ErpPurchaseCostAnalysisPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpPurchaseOrderDO>()
                .likeIfPresent(ErpPurchaseOrderDO::getOrderSn, reqVO.getOrderSn())
                .likeIfPresent(ErpPurchaseOrderDO::getOptRealname, reqVO.getOptRealname())
                .betweenIfPresent(ErpPurchaseOrderDO::getLxCreateTime, reqVO.getLxCreateTime())
                .geIfPresent(ErpPurchaseOrderDO::getCostReduction, reqVO.getCostReductionMin())
                .leIfPresent(ErpPurchaseOrderDO::getCostReduction, reqVO.getCostReductionMax())
                .orderByDesc(ErpPurchaseOrderDO::getLxCreateTime));
    }

}
