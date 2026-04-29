package cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 采购成本统计报表 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseCostReportRespVO {

    @Schema(description = "按采购员汇总的列表数据")
    private List<Item> items;

    @Schema(description = "总货物金额（全员合计），单位：元")
    private BigDecimal totalAmountGoodsAll;

    @Schema(description = "总降本金额（全员合计），单位：元")
    private BigDecimal totalCostReductionAll;

    @Schema(description = "统计起始时间")
    private java.time.LocalDateTime statBegin;

    @Schema(description = "统计截止时间")
    private java.time.LocalDateTime statEnd;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        @Schema(description = "采购员姓名")
        private String optRealname;

        @Schema(description = "总货物金额，单位：元")
        private BigDecimal totalAmountGoods;

        @Schema(description = "总货物金额占比（0-100）")
        private BigDecimal amountGoodsRatio;

        @Schema(description = "总降本金额，单位：元")
        private BigDecimal totalCostReduction;

        @Schema(description = "总降本金额占比（0-100）")
        private BigDecimal costReductionRatio;

    }

}
