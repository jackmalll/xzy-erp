package cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.xzy.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购成本统计报表 Request VO")
@Data
public class ErpPurchaseCostReportReqVO {

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "统计时间范围（采购创建日期）")
    private LocalDateTime[] createTime;

}
