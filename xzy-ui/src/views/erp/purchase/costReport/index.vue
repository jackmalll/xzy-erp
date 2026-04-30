<template>
  <ContentWrap>
    <div class="relative flex items-center justify-start flex-wrap gap-y-10px min-h-32px">
      <div class="flex items-center gap-8px flex-wrap">
        <el-radio-group v-model="quickRange" @change="handleQuickRange">
          <el-radio-button label="1m">近1个月</el-radio-button>
          <el-radio-button label="3m">近3个月</el-radio-button>
          <el-radio-button label="6m">近6个月</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-if="quickRange === 'custom'"
          v-model="customRange"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="!w-260px"
          @change="loadReport"
        />
      </div>
      <h2 class="absolute left-1/2 -translate-x-1/2 text-16px font-bold text-gray-800 m-0">采购成本统计报表</h2>
    </div>
  </ContentWrap>

  <ContentWrap v-loading="loading">
    <div v-if="reportData" class="flex gap-24px flex-wrap">
      <!-- 左侧：汇总表格 -->
      <div class="flex-1 min-w-400px">
        <div class="mb-12px text-13px text-gray-400 text-right">
          统计周期：{{ statPeriodText }}
        </div>
        <el-table :data="reportData.items" border>
          <el-table-column label="采购员" prop="optRealname" align="center" width="100" />
          <el-table-column label="总货物金额" align="center">
            <template #header>
              <div>总货物金额</div>
              <div class="text-12px text-gray-400">¥{{ formatMoney(reportData.totalAmountGoodsAll) }}</div>
            </template>
            <template #default="{ row }">
              <span>¥{{ formatMoney(row.totalAmountGoods) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" align="center" width="80">
            <template #default="{ row }">
              <span>{{ row.amountGoodsRatio }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="总降本金额" align="center">
            <template #header>
              <div>总降本金额</div>
              <div class="text-12px text-gray-400">¥{{ formatMoney(reportData.totalCostReductionAll) }}</div>
            </template>
            <template #default="{ row }">
              <span :class="getCostReductionClass(row.totalCostReduction)">
                ¥{{ formatMoney(row.totalCostReduction) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="占比" align="center" width="80">
            <template #default="{ row }">
              <span :class="getCostReductionClass(row.totalCostReduction)">{{ getCostReductionRatio(row) }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 右侧：三个饼图 -->
      <div class="flex gap-24px flex-wrap">
        <!-- 总货物金额 -->
        <div class="flex flex-col items-center">
          <div class="text-13px font-medium text-gray-700">总货物金额</div>
          <div class="text-12px text-gray-400 mb-4px">¥{{ formatMoney(reportData.totalAmountGoodsAll) }}</div>
          <Echart :options="goodsPieOptions" height="260px" width="320px" />
        </div>
        <!-- 降本（正） -->
        <div class="flex flex-col items-center">
          <div class="text-13px font-medium text-gray-700">总降本金额（正）</div>
          <div class="text-12px text-green-600 mb-4px">¥{{ formatMoney(totalPositiveCostReduction) }}</div>
          <template v-if="positivePieHasData">
            <Echart :options="positivePieOptions" height="260px" width="320px" />
          </template>
          <template v-else>
            <div class="flex items-center justify-center" style="height:260px;width:320px">
              <el-empty description="暂无正数降本" :image-size="60" />
            </div>
          </template>
        </div>
        <!-- 降本（负） -->
        <div class="flex flex-col items-center">
          <div class="text-13px font-medium text-gray-700">总降本金额（负）</div>
          <div class="text-12px text-red-500 mb-4px">¥{{ formatMoney(totalNegativeCostReduction) }}</div>
          <template v-if="negativePieHasData">
            <Echart :options="negativePieOptions" height="260px" width="320px" />
          </template>
          <template v-else>
            <div class="flex items-center justify-center" style="height:260px;width:320px">
              <el-empty description="暂无负数降本" :image-size="60" />
            </div>
          </template>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无数据" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import type { EChartsOption } from 'echarts'
import * as CostAnalysisApi from '@/api/erp/purchase/costAnalysis'
import Echart from '@/components/Echart/src/Echart.vue'

defineOptions({ name: 'ErpPurchaseCostReport' })

const loading = ref(false)
const quickRange = ref<'1m' | '3m' | '6m' | 'custom'>('1m')
const customRange = ref<string[]>([])
const reportData = ref<CostAnalysisApi.PurchaseCostReportVO | null>(null)
const currentRange = ref<string[]>([])

const PIE_COLORS = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
const POS_COLORS = ['#91cc75', '#3ba272', '#5470c6', '#73c0de', '#fac858', '#fc8452', '#9a60b4', '#ee6666']
const NEG_COLORS = ['#ee6666', '#fc8452', '#fac858', '#9a60b4', '#73c0de', '#5470c6', '#91cc75', '#3ba272']

const statPeriodText = computed(() => {
  if (!reportData.value) return ''
  const fmt = (s: string | null | undefined) => (s ? s.slice(0, 10) : '不限')
  return `${fmt(currentRange.value[0])} ~ ${fmt(currentRange.value[1])}`
})

const goodsPieOptions = computed<EChartsOption>(() => {
  const items = reportData.value?.items ?? []
  return {
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', right: 8, top: 'center', textStyle: { fontSize: 12 } },
    color: PIE_COLORS,
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['34%', '50%'],
        label: { show: false },
        data: items.map((item) => ({
          name: item.optRealname || '未知',
          value: Number(item.totalAmountGoods ?? 0)
        }))
      }
    ]
  }
})

const positivePieHasData = computed(() =>
  (reportData.value?.items ?? []).some((item) => Number(item.totalCostReduction ?? 0) > 0)
)

const negativePieHasData = computed(() =>
  (reportData.value?.items ?? []).some((item) => Number(item.totalCostReduction ?? 0) < 0)
)

const positivePieOptions = computed<EChartsOption>(() => {
  const items = (reportData.value?.items ?? []).filter(
    (item) => Number(item.totalCostReduction ?? 0) > 0
  )
  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => `${params.name}: ¥${params.value} (${params.percent}%)`
    },
    legend: { orient: 'vertical', right: 8, top: 'center', textStyle: { fontSize: 12 } },
    color: POS_COLORS,
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['34%', '50%'],
        label: { show: false },
        data: items.map((item) => ({
          name: item.optRealname || '未知',
          value: Number(item.totalCostReduction ?? 0)
        }))
      }
    ]
  }
})

const negativePieOptions = computed<EChartsOption>(() => {
  const items = (reportData.value?.items ?? []).filter(
    (item) => Number(item.totalCostReduction ?? 0) < 0
  )
  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) =>
        `${params.name}: ¥${(-Math.abs(params.value)).toFixed(2)} (${params.percent}%)`
    },
    legend: { orient: 'vertical', right: 8, top: 'center', textStyle: { fontSize: 12 } },
    color: NEG_COLORS,
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['34%', '50%'],
        label: { show: false },
        data: items.map((item) => ({
          name: item.optRealname || '未知',
          value: Math.abs(Number(item.totalCostReduction ?? 0))
        }))
      }
    ]
  }
})

const totalPositiveCostReduction = computed(() =>
  (reportData.value?.items ?? [])
    .filter((item) => Number(item.totalCostReduction ?? 0) > 0)
    .reduce((sum, item) => sum + Number(item.totalCostReduction ?? 0), 0)
)

const totalNegativeCostReduction = computed(() =>
  (reportData.value?.items ?? [])
    .filter((item) => Number(item.totalCostReduction ?? 0) < 0)
    .reduce((sum, item) => sum + Number(item.totalCostReduction ?? 0), 0)
)

const getCostReductionRatio = (row: any): string => {
  const val = Number(row.totalCostReduction ?? 0)
  if (val > 0) {
    const total = totalPositiveCostReduction.value
    return total === 0 ? '0' : ((val / total) * 100).toFixed(0)
  }
  if (val < 0) {
    const total = totalNegativeCostReduction.value
    return total === 0 ? '0' : ((val / total) * 100).toFixed(0)
  }
  return '0'
}

const getDateRange = (): string[] => {
  const now = new Date()
  const end = formatDateTime(now)
  if (quickRange.value === '1m') {
    const begin = new Date(now)
    begin.setMonth(begin.getMonth() - 1)
    return [formatDateTime(begin), end]
  }
  if (quickRange.value === '3m') {
    const begin = new Date(now)
    begin.setMonth(begin.getMonth() - 3)
    return [formatDateTime(begin), end]
  }
  if (quickRange.value === '6m') {
    const begin = new Date(now)
    begin.setMonth(begin.getMonth() - 6)
    return [formatDateTime(begin), end]
  }
  return customRange.value ?? []
}

const formatDateTime = (d: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} 00:00:00`
}

const formatMoney = (val?: number | null) => {
  if (val === undefined || val === null) return '0.00'
  return Number(val).toFixed(2)
}

const getCostReductionClass = (val?: number | null) => {
  if (!val) return ''
  return Number(val) > 0 ? 'text-green-600 font-medium' : 'text-red-500 font-medium'
}

const loadReport = async () => {
  loading.value = true
  try {
    const range = getDateRange()
    currentRange.value = range
    const params: { createTime?: string[] } = {}
    if (range.length === 2) params.createTime = range
    reportData.value = await CostAnalysisApi.getPurchaseCostReport(params)
  } finally {
    loading.value = false
  }
}

const handleQuickRange = (val: string) => {
  if (val !== 'custom') loadReport()
}

onMounted(() => {
  loadReport()
})
</script>
