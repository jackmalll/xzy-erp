package cn.xzy.module.erp.controller.admin.purchase.vo.costanalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "采购订单商品明细 VO（含历史采购价）")
@Data
public class ErpPurchaseOrderItemDetailVO {

    @Schema(description = "品名")
    private String productName;

    @Schema(description = "SKU")
    private String sku;

    @Schema(description = "采购数量")
    private Integer quantityReal;

    @Schema(description = "采购员")
    private String optRealname;

    @Schema(description = "本次采购含税单价")
    private BigDecimal price;

    @Schema(description = "本次采购日期")
    private LocalDateTime purchaseDate;

    @Schema(description = "产品图片链接")
    private String picUrl;

    @Schema(description = "基准单价（第一次历史采购含税单价），无历史则为 null")
    private BigDecimal basePrice;

    @Schema(description = "行降本金额 = (basePrice - price) × quantityReal，无基准价则为 null")
    private BigDecimal itemCostReduction;

    @Schema(description = "历史采购价列表（最多3条，排除本订单，按时间倒序）")
    private List<HistoryPrice> historyPrices;

    @Data
    public static class HistoryPrice {

        @Schema(description = "含税单价")
        private BigDecimal price;

        @Schema(description = "采购日期")
        private LocalDateTime purchaseDate;
    }

}
