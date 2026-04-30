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

    /**
     * 到货入库量
     */
    private Integer quantityEntry;

    /**
     * 产品图片链接（来自领星本地产品列表接口）
     */
    private String picUrl;

    /**
     * 基准单价（根据基准单价N配置规则计算，单位：元）
     */
    private BigDecimal basePrice;

    /**
     * 采购有效状态：0-待审核 1-有效 2-无效
     */
    private Integer itemStatus;

}
