import request from '@/config/axios'

export interface BasePriceConfigVO {
  ruleMinPrice: boolean | null
  ruleThreeEnabled: boolean | null
  ruleThreeCalcType: string | null
  rule1MaxCount: number | null
  rule1AvgFrom: number | null
  rule1AvgTo: number | null
  rule2MinCount: number | null
  rule2MaxCount: number | null
  rule2AvgFrom: number | null
  rule2AvgTo: number | null
  rule3MinCount: number | null
  rule3AvgFrom: number | null
  rule3AvgTo: number | null
  riskQtyEnabled: boolean | null
  riskQtyThreshold: number | null
  riskAmountEnabled: boolean | null
  riskAmountThreshold: number | null
  riskPriceFloatEnabled: boolean | null
  riskPriceFloatPercent: number | null
}

// 获取基准单价配置
export const getBasePriceConfig = () => {
  return request.get<BasePriceConfigVO>({ url: '/erp/base-price-config/get' })
}

// 保存基准单价配置
export const saveBasePriceConfig = (data: BasePriceConfigVO) => {
  return request.post({ url: '/erp/base-price-config/save', data })
}
