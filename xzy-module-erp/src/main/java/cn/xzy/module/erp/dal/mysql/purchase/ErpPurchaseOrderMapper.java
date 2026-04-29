package cn.xzy.module.erp.dal.mysql.purchase;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisPageReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostReportRespVO;
import cn.xzy.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 按采购员汇总货物金额与降本金额（用于采购成本统计报表）
     *
     * @param beginTime 统计开始时间（可为 null）
     * @param endTime   统计截止时间（可为 null）
     * @return 每个采购员的汇总行
     */
    @Select("<script>" +
            "SELECT opt_realname AS optRealname," +
            " SUM(amount_total) AS totalAmountGoods," +
            " SUM(cost_reduction) AS totalCostReduction" +
            " FROM erp_purchase_order" +
            " WHERE deleted = 0" +
            "<if test='beginTime != null'> AND lx_create_time &gt;= #{beginTime}</if>" +
            "<if test='endTime != null'> AND lx_create_time &lt;= #{endTime}</if>" +
            " GROUP BY opt_realname" +
            " ORDER BY totalAmountGoods DESC" +
            "</script>")
    List<ErpPurchaseCostReportRespVO.Item> selectCostReportGroupByBuyer(
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

}
