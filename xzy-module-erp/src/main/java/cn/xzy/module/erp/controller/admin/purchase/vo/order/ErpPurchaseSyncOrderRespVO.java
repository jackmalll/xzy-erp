package cn.xzy.module.erp.controller.admin.purchase.vo.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领星采购订单主体 VO（对应接口返回 data 数组元素）
 */
@Data
public class ErpPurchaseSyncOrderRespVO {

    /**
     * 采购单号
     */
    @JsonProperty("order_sn")
    private String orderSn;

    /**
     * 采购员姓名
     */
    @JsonProperty("opt_realname")
    private String optRealname;

    /**
     * 创建时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    @JsonProperty("create_time")
    private String createTime;

    /**
     * 最后操作时间
     */
    @JsonProperty("last_time")
    private String lastTime;

    /**
     * 应付货款（手工）
     */
    @JsonProperty("payment")
    private BigDecimal payment;

    /**
     * 是否含税：0否 1是
     */
    @JsonProperty("is_tax")
    private Integer isTax;

    /**
     * 采购单状态
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 到货状态：1未到货 2部分到货 3全部到货
     */
    @JsonProperty("status_shipped")
    private Integer statusShipped;

    /**
     * 运费
     */
    @JsonProperty("shipping_price")
    private BigDecimal shippingPrice;

    /**
     * 采购子项列表
     */
    @JsonProperty("item_list")
    private List<ErpPurchaseSyncOrderItemRespVO> itemList;

}
