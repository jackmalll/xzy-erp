-- 将菜单"销售利润报表"改为"基准单价配置"，换成配置相关图标，并绑定前端组件
-- icon 使用 ep:setting（设置图标），符合"配置"语义
UPDATE system_menu
SET name           = '基准单价配置',
    icon           = 'ep:setting',
    permission     = 'erp:base-price-config:query',
    component      = 'erp/purchase/basePriceConfig/index',
    component_name = 'ErpBasePriceConfig',
    path           = 'base-price-config'
WHERE name = '销售利润报表';
