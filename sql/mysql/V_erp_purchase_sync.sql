-- ====================== 采购订单主表 重建脚本 ======================
DROP TABLE IF EXISTS `erp_purchase_order`;
-- 采购订单主表（来源：领星 ERP OpenAPI）
CREATE TABLE `erp_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '领星采购单号（唯一键）',
  `opt_realname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '采购员姓名',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '采购单状态：-1作废 1待下单 2待签收 3待提交 9完成 121待审核 122驳回 124作废',
  `status_shipped` tinyint NOT NULL DEFAULT '1' COMMENT '到货状态：1未到货 2部分到货 3全部到货',
  `is_tax` tinyint NOT NULL DEFAULT '0' COMMENT '是否含税：0否 1是',
  `amount_total` decimal(12,2) DEFAULT NULL COMMENT '应付货款（手工）',
  `cost_reduction` decimal(12,2) DEFAULT NULL COMMENT '降本金额（元），同步时计算写入',
  `shipping_price` decimal(12,2) DEFAULT NULL COMMENT '运费',
  `lx_create_time` datetime DEFAULT NULL COMMENT '领星创建时间',
  `lx_last_time` datetime DEFAULT NULL COMMENT '领星最后操作时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_sn` (`order_sn`) USING BTREE,
  KEY `idx_cost_reduction` (`cost_reduction`)
) ENGINE=InnoDB AUTO_INCREMENT=4525 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购订单主表（来源：领星 ERP）';

-- ====================== 采购订单子项表 重建脚本 ======================
DROP TABLE IF EXISTS `erp_purchase_order_item`;
-- 采购订单子项表（来源：领星 ERP OpenAPI）
CREATE TABLE `erp_purchase_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_sn` varchar(64) NOT NULL COMMENT '所属采购单号',
  `pic_url` varchar(512) DEFAULT NULL COMMENT '产品图片链接（来自领星本地产品列表接口）',
  `lx_item_id` bigint NOT NULL COMMENT '领星子项 id',
  `product_name` varchar(256) DEFAULT NULL COMMENT '品名',
  `sku` varchar(128) DEFAULT NULL COMMENT 'SKU',
  `price` decimal(14,4) DEFAULT NULL COMMENT '含税单价',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '价税合计',
  `quantity_real` int NOT NULL DEFAULT '0' COMMENT '实际采购量',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lx_item_id` (`lx_item_id`),
  KEY `idx_order_sn` (`order_sn`),
  KEY `idx_sku` (`sku`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单子项表（来源：领星 ERP）';