package cn.xzy.module.erp.controller.admin.purchase.vo.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领星采购订单子项 VO（对应接口返回 item_list 数组元素）
 */
@Data
public class ErpPurchaseSyncOrderItemRespVO {

    /**
     * 领星子项 id
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 品名
     */
    @JsonProperty("product_name")
    private String productName;

    /**
     * SKU
     */
    @JsonProperty("sku")
    private String sku;

    /**
     * 含税单价
     */
    @JsonProperty("price")
    private BigDecimal price;

    /**
     * 价税合计
     */
    @JsonProperty("amount")
    private BigDecimal amount;

    /**
     * 实际采购量
     */
    @JsonProperty("quantity_real")
    private Integer quantityReal;

}
