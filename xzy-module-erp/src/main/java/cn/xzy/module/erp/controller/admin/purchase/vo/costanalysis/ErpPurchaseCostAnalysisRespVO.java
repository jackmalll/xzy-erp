package cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 采购成本统计分析 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseCostAnalysisRespVO {

    @Schema(description = "采购订单单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PO260413888")
    @ExcelProperty("采购订单单号")
    private String orderSn;

    @Schema(description = "采购员姓名", example = "张三")
    @ExcelProperty("采购员")
    private String optRealname;

    @Schema(description = "采购创建日期")
    @ExcelProperty("采购创建日期")
    private LocalDateTime lxCreateTime;

    @Schema(description = "采购状态", example = "9")
    @ExcelProperty("采购状态")
    private Integer status;

    @Schema(description = "采购完成日期（最后操作时间）")
    @ExcelProperty("采购完成日期")
    private LocalDateTime lxLastTime;

    @Schema(description = "包含SKU数量（子项种类数）", example = "10")
    @ExcelProperty("包含SKU数量")
    private Integer skuCount;

    @Schema(description = "货物总价，单位：元", example = "900.00")
    @ExcelProperty("货物总价")
    private BigDecimal amountTotal;

    @Schema(description = "采购实付金额（应付货款+运费），单位：元", example = "975.00")
    @ExcelProperty("采购实付金额")
    private BigDecimal actualPayment;

    @Schema(description = "降本金额，单位：元", example = "95.66")
    @ExcelProperty("降本金额")
    private BigDecimal costReduction;

}
