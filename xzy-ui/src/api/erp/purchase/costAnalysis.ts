import request from '@/config/axios'

export interface PurchaseCostAnalysisVO {
  orderSn: string
  optRealname: string
  lxCreateTime: Date
  status: number
  lxLastTime: Date
  skuCount: number
  amountTotal: number
  actualPayment: number
  costReduction: number
}

export interface HistoryPriceVO {
  price: number
  purchaseDate: string
}

export interface OrderItemDetailVO {
  productName: string
  sku: string
  quantityReal: number
  optRealname: string
  price: number
  purchaseDate: string
  picUrl: string
  basePrice: number | null
  itemCostReduction: number | null
  historyPrices: HistoryPriceVO[]
}

// 获得采购成本统计分析分页
export const getPurchaseCostAnalysisPage = (params: PageParam) => {
  return request.get({ url: '/erp/purchase-cost-analysis/page', params })
}

// 获得采购订单商品明细（含历史采购价）
export const getOrderItemDetails = (orderSn: string) => {
  return request.get({ url: '/erp/purchase-cost-analysis/order-items', params: { orderSn } })
}

// 导出采购成本统计分析 Excel
export const exportPurchaseCostAnalysis = (params: any) => {
  return request.download({ url: '/erp/purchase-cost-analysis/export-excel', params })
}
