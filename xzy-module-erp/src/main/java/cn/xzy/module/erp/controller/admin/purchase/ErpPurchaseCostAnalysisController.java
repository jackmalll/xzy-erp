package cn.xzy.module.erp.controller.admin.purchase;

import cn.xzy.framework.apilog.core.annotation.ApiAccessLog;
import cn.xzy.framework.common.pojo.CommonResult;
import cn.xzy.framework.common.pojo.PageParam;
import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.excel.core.util.ExcelUtils;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisPageReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostAnalysisRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostReportReqVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseCostReportRespVO;
import cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis.ErpPurchaseOrderItemDetailVO;
import cn.xzy.module.erp.service.purchase.ErpPurchaseCostAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.xzy.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.xzy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 采购成本统计分析")
@RestController
@RequestMapping("/erp/purchase-cost-analysis")
@Validated
public class ErpPurchaseCostAnalysisController {

    @Resource
    private ErpPurchaseCostAnalysisService purchaseCostAnalysisService;

    @GetMapping("/page")
    @Operation(summary = "获得采购成本统计分析分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-cost-analysis:query')")
    public CommonResult<PageResult<ErpPurchaseCostAnalysisRespVO>> getPurchaseCostAnalysisPage(
            @Valid ErpPurchaseCostAnalysisPageReqVO pageReqVO) {
        PageResult<ErpPurchaseCostAnalysisRespVO> pageResult =
                purchaseCostAnalysisService.getPurchaseCostAnalysisPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/order-items")
    @Operation(summary = "获得采购订单商品明细（含历史采购价）")
    @Parameter(name = "orderSn", description = "采购订单号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-cost-analysis:query')")
    public CommonResult<List<ErpPurchaseOrderItemDetailVO>> getOrderItemDetails(
            @RequestParam("orderSn") String orderSn) {
        return success(purchaseCostAnalysisService.getOrderItemDetails(orderSn));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购成本统计分析 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-cost-analysis:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseCostAnalysisExcel(@Valid ErpPurchaseCostAnalysisPageReqVO exportReqVO,
                                                HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseCostAnalysisRespVO> list =
                purchaseCostAnalysisService.getPurchaseCostAnalysisPage(exportReqVO).getList();
        ExcelUtils.write(response, "采购成本统计分析.xls", "数据", ErpPurchaseCostAnalysisRespVO.class, list);
    }

    @GetMapping("/cost-report")
    @Operation(summary = "获得采购成本统计报表（按采购员汇总）")
    @PreAuthorize("@ss.hasPermission('erp:purchase-cost-analysis:query')")
    public CommonResult<ErpPurchaseCostReportRespVO> getPurchaseCostReport(ErpPurchaseCostReportReqVO reqVO) {
        return success(purchaseCostAnalysisService.getPurchaseCostReport(reqVO));
    }

    @PostMapping("/recalc-cost-reduction")
    @Operation(summary = "【临时】补算所有存量订单降本金额，执行一次后可删除此接口")
    @PreAuthorize("@ss.hasPermission('erp:purchase-cost-analysis:query')")
    public CommonResult<Integer> recalcAllCostReduction() {
        return success(purchaseCostAnalysisService.recalcAllCostReduction());
    }

}
