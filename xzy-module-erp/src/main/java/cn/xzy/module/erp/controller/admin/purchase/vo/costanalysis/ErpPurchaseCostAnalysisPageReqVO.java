package cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis;

import cn.xzy.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.xzy.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购成本统计分析分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ErpPurchaseCostAnalysisPageReqVO extends PageParam {

    @Schema(description = "采购订单号，模糊匹配", example = "PO260413888")
    private String orderSn;

    @Schema(description = "采购员姓名，模糊匹配", example = "张三")
    private String optRealname;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "采购创建日期范围")
    private LocalDateTime[] lxCreateTime;

    @Schema(description = "降本金额最小值（含），单位：元", example = "100.00")
    private BigDecimal costReductionMin;

    @Schema(description = "降本金额最大值（含），单位：元", example = "500.00")
    private BigDecimal costReductionMax;

}
