package cn.xzy.module.erp.dal.dataobject.purchase;

import cn.xzy.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 采购订单子项 DO
 */
@TableName("erp_purchase_order_item")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseOrderItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 所属采购单号
     *
     * 关联 {@link ErpPurchaseOrderDO#getOrderSn()}
     */
    private String orderSn;

    /**
     * 领星子项 id（唯一键）
     */
    private Long lxItemId;

    /**
     * 品名
     */
    private String productName;

    /**
     * SKU
     */
    private String sku;

    /**
     * 含税单价，单位：元
     */
    private BigDecimal price;

    /**
     * 价税合计，单位：元
     */
    private BigDecimal amount;

    /**
     * 实际采购量
     */
    private Integer quantityReal;

}
