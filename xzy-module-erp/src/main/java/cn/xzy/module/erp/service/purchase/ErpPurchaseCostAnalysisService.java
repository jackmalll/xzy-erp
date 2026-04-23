package cn.xzy.module.erp.service.purchase;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisPageReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;

import java.util.List;

/**
 * 采购成本统计分析 Service 接口
 */
public interface ErpPurchaseCostAnalysisService {

    /**
     * 获得采购成本统计分析分页
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    PageResult<ErpPurchaseCostAnalysisRespVO> getPurchaseCostAnalysisPage(ErpPurchaseCostAnalysisPageReqVO pageReqVO);

    /**
     * 获得某采购订单下的商品明细（含历史采购价）
     *
     * @param orderSn 采购订单号
     * @return 商品明细列表
     */
    List<ErpPurchaseOrderItemDetailVO> getOrderItemDetails(String orderSn);

    /**
     * 补算所有存量订单的降本金额并回写（一次性操作，执行后可删除此接口）
     *
     * @return 处理条数
     */
    int recalcAllCostReduction();

}
