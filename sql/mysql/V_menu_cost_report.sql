-- 将菜单"成本费用报表"改为"采购成本统计报表"，并绑定前端组件
UPDATE system_menu
SET name           = '采购成本统计报表',
    permission     = 'erp:purchase-cost-analysis:query',
    component      = 'erp/purchase/costReport/index',
    component_name = 'ErpPurchaseCostReport'
WHERE id = 5013;
