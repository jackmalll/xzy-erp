## [接口信息](https://apidoc.lingxing.com/#/docs/Purchase/PurchaseOrderList?id=接口信息)

| API Path                                                 | 请求协议 | 请求方式 | [令牌桶容量](https://apidoc.lingxing.com/#/docs/Guidance/newInstructions?id=_5-限流算法说明) |
| :------------------------------------------------------- | :------- | :------- | :----------------------------------------------------------- |
| `/erp/sc/routing/data/local_inventory/purchaseOrderList` | HTTPS    | POST     | 1                                                            |

## [请求参数](https://apidoc.lingxing.com/#/docs/Statistics/PurchaseReportProductList?id=请求参数)



| 参数名            | 说明                                                         | 必填 | 类型     | 示例        |
| :---------------- | :----------------------------------------------------------- | :--- | :------- | :---------- |
| start_date        | 开始时间，格式：Y-m-d，双闭区间 当筛选更新时间时，支持Y-m-d或Y-m-d H:i:s | 是   | [string] | 2024-05-30  |
| end_date          | 结束时间，格式：Y-m-d，双闭区间 当筛选更新时间时，支持Y-m-d或Y-m-d H:i:s | 是   | [string] | 2024-08-02  |
| search_field_time | 时间搜索维度： create_time 创建时间【默认值】 expect_arrive_time 预计到货时间 update_time 更新时间 | 否   | [string] | create_time |
| offset            | 分页偏移量，默认0                                            | 否   | [int]    | 0           |
| length            | 分页长度，默认500，上限500                                   | 否   | [int]    | 500         |



## [返回结果](https://apidoc.lingxing.com/#/docs/Purchase/PurchaseOrderList?id=返回结果)

Json Object

| 参数名                                  | 说明                                                         | 必填 | 类型     | 示例                                 |
| :-------------------------------------- | :----------------------------------------------------------- | :--- | :------- | :----------------------------------- |
| code                                    | 状态码，0 成功                                               | 是   | [int]    | 0                                    |
| message                                 | 消息提示                                                     | 是   | [string] | success                              |
| error_details                           | 错误信息                                                     | 是   | [array]  |                                      |
| request_id                              | 请求链路id                                                   | 是   | [string] | 929A1D7D-D656-0EA0-C78A-A686790C7090 |
| response_time                           | 响应时间                                                     | 是   | [string] | 2022-05-20 16:57:03                  |
| data                                    | 响应数据                                                     | 是   | [array]  |                                      |
| data>>order_sn                          | 采购单号                                                     | 是   | [string] | PO201019001                          |
| data>>opt_realname                      | 采购员姓名                                                   | 是   | [string] | 测试人员                             |
| data>>create_time                       | 创建时间                                                     | 是   | [string] | 2020-10-19 10:43:01                  |
| data>>amount_total                      | 货物总价                                                     | 是   | [number] | 0.00                                 |
| data>>last_time                         | 最后操作时间                                                 | 是   | [string] | 2020-10-19 10:43:01                  |
| data>>is_tax                            | 是否含税：0 否，1 是                                         | 是   | [int]    |                                      |
| data>>status                            | 采购单状态： -1 作废 3 待提交 1 待下单 - 已审核 2 待签收(待到货) - 已下单 9 完成 121 (审批流)待审核 122 (审批流)驳回 124 (审批流)作废 | 是   | [int]    | 1                                    |
| data>>status_shipped                    | 到货状态： 1 未到货 2 部分到货 3 全部到货                    | 是   | [int]    | 1                                    |
| data>>shipping_price                    | 运费                                                         | 是   | [number] | 0.00                                 |
| data>>pay_status                        | 付款状态： 0 未申请 1 已申请 2 部分付款 3 已付款             | 是   | [int]    | 1                                    |
| data>>quantity_entry                    | 入库量                                                       | 是   | [int]    | 10                                   |
| data>>quantity_real                     | 实际采购量                                                   | 是   | [int]    | 200                                  |
| data>>item_list                         | 采购单子项                                                   | 是   | [array]  |                                      |
| data>>item_list>>id                     | 采购单子项id                                                 | 是   | [int]    | 112                                  |
| data>>item_list>>product_name           | 品名                                                         | 是   | [string] | 铅笔                                 |
| data>>item_list>>sku                    | SKU                                                          | 是   | [string] | WYLS003                              |
| data>>item_list>>price                  | 含税单价                                                     | 是   | [number] | 1.0000                               |
| data>>item_list>>amount                 | 价税合计                                                     | 是   | [number] | 100.00                               |
| data>>item_list>>quantity_real          | 实际采购量                                                   | 是   | [int]    | 100                                  |
| data>>item_list>>quantity_entry         | 到货入库量                                                   | 是   | [int]    | 10                                   |
| data>>logistics_info>>purchase_order_sn | 采购订单号（order_sn）                                       | 是   | [string] |                                      |

---

## 同步完善说明

### 完善点 1：实际总价计算

- 接口返回 `data>>quantity_real`（实际采购量）与 `data>>quantity_entry`（入库量）已映射到 `erp_purchase_order` 的 `quantity_real` / `quantity_entry` 字段。
- 写入 `amount_total` 的规则：
  - 若 `quantity_real == quantity_entry`：直接使用接口返回的 `amount_total`。
  - 若 `quantity_real != quantity_entry`（部分到货）：`amount_total = SUM(item.price × item.quantity_entry)`（各子项到货入库量乘以含税单价之和）。

### 完善点 2：子项基准单价 & 有效状态

**新增字段（`erp_purchase_order_item`）：**

| 字段          | 类型          | 说明                                                       |
| :------------ | :------------ | :--------------------------------------------------------- |
| `quantity_entry` | INT        | 到货入库量（来自接口 `item_list>>quantity_entry`）         |
| `base_price`  | DECIMAL(12,4) | 基准单价（根据基准单价 N 配置规则计算）                    |
| `item_status` | TINYINT       | 采购有效状态：`1`-有效 / `2`-无效/忽略 / `0`-待审核       |

**基准单价（`base_price`）计算规则：**

取同一采购员对该 SKU 在本订单之前的历史采购单价列表（正序），按 `erp.basePriceConfig` 配置的规则计算：
- **最小值规则**（`ruleMinPrice=true`）：取全部历史最低价。
- **三段式规则**（`ruleThreeEnabled=true`）：根据历史次数落入规则1/2/3区间，取对应次序范围内价格的均值或最小值（`ruleThreeCalcType=avg/min`）。
- 若未配置任何规则或无历史记录，则 `base_price = null`。

**有效状态（`item_status`）判断逻辑：**

仅勾选（启用）的风控规则才参与判断，命中**无效**优先于**待审核**：

| 优先级 | 规则                                      | 触发条件                                               | 结果     |
| :----- | :---------------------------------------- | :----------------------------------------------------- | :------- |
| 1      | 采购 SKU 数量风控（`riskQtyEnabled`）      | `quantity_real < riskQtyThreshold`                     | `2`-无效 |
| 2      | 采购 SKU 总金额风控（`riskAmountEnabled`） | `amount < riskAmountThreshold`                         | `2`-无效 |
| 3      | 单价浮动风控（`riskPriceFloatEnabled`）    | `|price - base_price| / base_price × 100 > riskPriceFloatPercent%` | `0`-待审核 |
| -      | 均未命中                                  | -                                                      | `1`-有效 |