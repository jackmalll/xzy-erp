package cn.xzy.module.erp.dal.dataobject.purchase;

import cn.xzy.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购订单 DO
 */
@TableName("erp_purchase_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 采购单号（领星 order_sn，唯一键）
     */
    private String orderSn;

    /**
     * 采购员姓名
     */
    private String optRealname;

    /**
     * 采购单状态：-1作废 1待下单 2待签收 3待提交 9完成 121待审核 122驳回 124作废
     */
    private Integer status;

    /**
     * 到货状态：1未到货 2部分到货 3全部到货
     */
    private Integer statusShipped;

    /**
     * 是否含税：0否 1是
     */
    private Integer isTax;

    /**
     * 货物总价，单位：元
     */
    private BigDecimal amountTotal;

    /**
     * 运费，单位：元
     */
    private BigDecimal shippingPrice;

    /**
     * 领星创建时间
     */
    private LocalDateTime lxCreateTime;

    /**
     * 领星最后操作时间
     */
    private LocalDateTime lxLastTime;

    /**
     * 实际采购量（整单）
     */
    private Integer quantityReal;

    /**
     * 入库量（整单）
     */
    private Integer quantityEntry;

    /**
     * 降本金额（元），同步时计算写入
     */
    private BigDecimal costReduction;

}
