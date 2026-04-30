package cn.xzy.module.erp.controller.admin.purchase.vo.basepriceconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 基准单价（N）配置 VO
 * 规则配置 + 风控配置，整体作为 JSON 存入 infra_config 表
 */
@Schema(description = "管理后台 - 基准单价（N）配置 VO")
@Data
public class ErpBasePriceConfigVO {

    // ======================== 规则配置 ========================

    @Schema(description = "是否启用\"最小值\"规则：N = SKU所有历史采购单价中的最小值")
    private Boolean ruleMinPrice;

    @Schema(description = "是否启用三段式规则（规则1/2/3 统一开关），与最小值规则互斥")
    private Boolean ruleThreeEnabled;

    @Schema(description = "三段式规则计算方式：avg=均值（默认），min=最小值")
    private String ruleThreeCalcType;

    @Schema(description = "规则1：历史采购次数上限（默认3）")
    private Integer rule1MaxCount;

    @Schema(description = "规则1：均值起始次序（默认1）")
    private Integer rule1AvgFrom;

    @Schema(description = "规则1：均值结束次序（默认3）")
    private Integer rule1AvgTo;

    @Schema(description = "规则2：次数下限（默认3）")
    private Integer rule2MinCount;

    @Schema(description = "规则2：次数上限（默认10）")
    private Integer rule2MaxCount;

    @Schema(description = "规则2：均值起始次序（默认5）")
    private Integer rule2AvgFrom;

    @Schema(description = "规则2：均值结束次序（默认8）")
    private Integer rule2AvgTo;

    @Schema(description = "规则3：次数下限（默认10）")
    private Integer rule3MinCount;

    @Schema(description = "规则3：均值起始次序（默认5）")
    private Integer rule3AvgFrom;

    @Schema(description = "规则3：均值结束次序（默认8）；-1 表示取到最新一次（动态末尾）")
    private Integer rule3AvgTo;

    // ======================== 风控配置 ========================

    @Schema(description = "是否启用\"采购SKU数量\"风控")
    private Boolean riskQtyEnabled;

    @Schema(description = "风控：采购SKU数量阈值，低于此值标记无效忽略")
    private Integer riskQtyThreshold;

    @Schema(description = "是否启用\"采购SKU总金额\"风控")
    private Boolean riskAmountEnabled;

    @Schema(description = "风控：采购SKU总金额阈值，低于此值标记无效忽略")
    private BigDecimal riskAmountThreshold;

    @Schema(description = "是否启用\"单价浮动\"风控")
    private Boolean riskPriceFloatEnabled;

    @Schema(description = "风控：单价浮动百分比阈值（%），超过此值标记待审核")
    private BigDecimal riskPriceFloatPercent;

}
