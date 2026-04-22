<template>
  <ContentWrap>
    <el-form
      :model="queryParams"
      ref="queryFormRef"
      label-width="90px"
    >
      <div class="flex items-center justify-between flex-wrap gap-y-10px">
        <div class="flex items-center flex-wrap gap-y-10px">
          <el-form-item label="采购订单号" prop="orderSn" class="!mb-0">
            <el-input
              v-model="queryParams.orderSn"
              placeholder="请输入采购订单号"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="采购人" prop="optRealname" class="!mb-0">
            <el-input
              v-model="queryParams.optRealname"
              placeholder="请输入采购人"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="采购创建日期" prop="lxCreateTime" label-width="110px" class="!mb-0">
            <el-date-picker
              v-model="queryParams.lxCreateTime"
              type="daterange"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="降本金额" label-width="80px" class="!mb-0">
            <div class="flex items-center gap-4px">
              <el-input-number
                v-model="queryParams.costReductionMin"
                placeholder="最小值"
                :controls="false"
                class="!w-110px"
                @keyup.enter="handleQuery"
              />
              <span class="text-gray-400">~</span>
              <el-input-number
                v-model="queryParams.costReductionMax"
                placeholder="最大值"
                :controls="false"
                class="!w-110px"
                @keyup.enter="handleQuery"
              />
            </div>
          </el-form-item>
        </div>
        <div class="flex items-center gap-8px flex-shrink-0">
          <el-button type="primary" plain @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button
            type="success"
            plain
            @click="handleExport"
            :loading="exportLoading"
            v-hasPermi="['erp:purchase-cost-analysis:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="mb-10px text-right text-gray-500 text-13px">本页面单价为含税单价</div>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="采购订单单号" align="center" prop="orderSn" min-width="140" />
      <el-table-column label="采购员" align="center" prop="optRealname" width="100" />
      <el-table-column
        label="采购创建日期"
        align="center"
        prop="lxCreateTime"
        width="120"
        :formatter="(row) => formatDate(row.lxCreateTime)"
      />
      <el-table-column label="采购状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="getPurchaseStatusType(row.status)">
            {{ getPurchaseStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="采购完成日期"
        align="center"
        prop="lxLastTime"
        width="120"
        :formatter="(row) => formatDate(row.lxLastTime)"
      />
      <el-table-column label="包含SKU数量" align="center" prop="skuCount" width="110" />
      <el-table-column label="货物总价" align="center" prop="amountTotal" width="130">
        <template #default="{ row }">
          <span>¥{{ formatMoney(row.amountTotal) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="降本金额" align="center" prop="costReduction" width="110">
        <template #default="{ row }">
          <span :class="row.costReduction > 0 ? 'text-green-600' : row.costReduction < 0 ? 'text-red-500' : ''">
            ¥{{ formatMoney(row.costReduction) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleViewDetail(row)"
            v-hasPermi="['erp:purchase-cost-analysis:query']"
          >
            查看统计明细
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 统计明细弹窗 -->
  <el-dialog
    v-model="detailVisible"
    title="采购订单包含商品统计明细"
    width="1100px"
    destroy-on-close
    :close-on-click-modal="false"
    @closed="handleDetailClosed"
  >
    <div class="mb-12px text-13px text-gray-500">本页面单价为含税单价</div>

    <el-table
      v-loading="detailLoading"
      :data="detailItems"
      border
      stripe
      style="width: 100%"
    >
      <!-- 图片 -->
      <el-table-column label="图片" width="80" align="center">
        <template #default="{ row, $index }">
          <el-popover
            v-if="row.picUrl"
            placement="right"
            :width="isPreviewExpanded(row, $index) ? 520 : 340"
            :visible="previewVisibleKey === getPreviewKey(row, $index)"
            popper-class="purchase-image-popover"
            :teleported="true"
          >
            <template #reference>
              <div
                class="purchase-image-trigger"
                :class="{ 'is-locked': isPreviewExpanded(row, $index) }
                "
                @mouseenter="handlePreviewReferenceEnter(row, $index)"
                @mouseleave="handlePreviewReferenceLeave(row, $index)"
                @click="handlePreviewClick(row, $index)"
              >
                <img :src="row.picUrl" alt="商品图片" class="purchase-image-thumb" />
              </div>
            </template>

            <div
              class="purchase-image-popover-content"
              :class="{ 'is-expanded': isPreviewExpanded(row, $index) }"
              @mouseenter="handlePreviewPopoverEnter()"
              @mouseleave="handlePreviewPopoverLeave(row, $index)"
            >
              <img
                :src="row.picUrl"
                alt="商品大图"
                class="purchase-image-large"
                :class="{ 'is-expanded': isPreviewExpanded(row, $index) }"
                @click="handlePreviewImageClick(row, $index)"
              />
              <div v-if="!isPreviewExpanded(row, $index)" class="purchase-image-actions">
                <el-button size="small" @click.stop="handlePreviewButtonClick(row, $index)">
                  查看原图
                </el-button>
              </div>
            </div>
          </el-popover>
          <div
            v-else
            style="
              width: 48px; height: 48px; line-height: 48px; text-align: center;
              border: 1px solid #d1d5db; border-radius: 4px;
              background: #fafafa; color: #9ca3af; font-size: 12px;
              margin: auto;
            "
          >无图</div>
        </template>
      </el-table-column>

      <!-- 品名/SKU/数量/采购员 -->
      <el-table-column label="品名" prop="productName" min-width="220">
        <template #default="{ row }">
          <div class="text-sm leading-snug">{{ row.productName || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="SKU" prop="sku" width="120" align="center" />
      <el-table-column label="采购数量" prop="quantityReal" width="90" align="center" />
      <el-table-column label="采购员" prop="optRealname" width="90" align="center" />

      <!-- 本次采购价 -->
      <el-table-column align="center" min-width="110">
        <template #header>
          <div class="inline-block px-2 py-0.5 rounded text-white text-xs" style="background:#f87171;">本次采购价</div>
          <div class="text-xs text-gray-500 mt-0.5">采购单价</div>
        </template>
        <template #default="{ row }">
          <div class="font-medium">¥{{ formatPrice(row.price) }}</div>
          <div class="text-xs text-gray-400 mt-0.5">{{ formatDate(row.purchaseDate) }}</div>
        </template>
      </el-table-column>

      <!-- 历史采购价：第1次 -->
      <el-table-column align="center" min-width="110">
        <template #header>
          <div class="inline-block px-2 py-0.5 rounded text-white text-xs" style="background:#60a5fa;">历史采购价</div>
          <div class="text-xs text-gray-500 mt-0.5">第1次采购单价/日期</div>
        </template>
        <template #default="{ row }">
          <template v-if="row.historyPrices && row.historyPrices.length >= 1">
            <div class="font-medium">¥{{ formatPrice(row.historyPrices[0].price) }}</div>
            <div class="text-xs text-gray-400 mt-0.5">{{ formatDate(row.historyPrices[0].purchaseDate) }}</div>
          </template>
          <template v-else>
            <span class="text-blue-400 text-sm">首次采购</span>
          </template>
        </template>
      </el-table-column>

      <!-- 历史采购价：第2次 -->
      <el-table-column align="center" min-width="110">
        <template #header>
          <div class="inline-block px-2 py-0.5 rounded text-white text-xs" style="background:#60a5fa;">历史采购价</div>
          <div class="text-xs text-gray-500 mt-0.5">第2次采购单价/日期</div>
        </template>
        <template #default="{ row }">
          <template v-if="row.historyPrices && row.historyPrices.length >= 2">
            <div class="font-medium">¥{{ formatPrice(row.historyPrices[1].price) }}</div>
            <div class="text-xs text-gray-400 mt-0.5">{{ formatDate(row.historyPrices[1].purchaseDate) }}</div>
          </template>
          <template v-else>
            <span class="text-gray-300">-</span>
          </template>
        </template>
      </el-table-column>

      <!-- 历史采购价：第3次 -->
      <el-table-column align="center" min-width="110">
        <template #header>
          <div class="inline-block px-2 py-0.5 rounded text-white text-xs" style="background:#60a5fa;">历史采购价</div>
          <div class="text-xs text-gray-500 mt-0.5">第3次采购单价/日期</div>
        </template>
        <template #default="{ row }">
          <template v-if="row.historyPrices && row.historyPrices.length >= 3">
            <div class="font-medium">¥{{ formatPrice(row.historyPrices[2].price) }}</div>
            <div class="text-xs text-gray-400 mt-0.5">{{ formatDate(row.historyPrices[2].purchaseDate) }}</div>
          </template>
          <template v-else>
            <span class="text-gray-300">-</span>
          </template>
        </template>
      </el-table-column>

      <!-- 展开更多历史记录 -->
      <el-table-column label="" width="100" align="center">
        <template #header>
          <el-button
            link
            type="primary"
            size="small"
            @click="toggleAllExpand"
          >
            {{ allExpanded ? '折叠全部' : '展开全部' }}
          </el-button>
        </template>
        <template #default="{ row, $index }">
          <el-button
            v-if="row.historyPrices && row.historyPrices.length > 3"
            link
            type="primary"
            size="small"
            @click="toggleRowExpand($index)"
          >
            {{ expandedRows.has($index) ? '折叠' : '展开更多' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 展开行：展示第4条及以后的历史价格 -->
    <template v-for="(row, idx) in detailItems" :key="'expand-' + idx">
      <div
        v-if="expandedRows.has(idx) && row.historyPrices && row.historyPrices.length > 3"
        class="mx-1 mb-2 p-3 bg-blue-50 border border-blue-100 rounded"
      >
        <div class="text-xs text-gray-500 mb-2 font-medium">【{{ row.sku }}】更多历史采购价</div>
        <div class="flex flex-wrap gap-4">
          <div
            v-for="(hp, hIdx) in row.historyPrices.slice(3)"
            :key="hIdx"
            class="text-sm"
          >
            <span class="text-gray-500">第{{ hIdx + 4 }}次：</span>
            <span class="font-medium">¥{{ formatPrice(hp.price) }}</span>
            <span class="text-gray-400 ml-1">{{ formatDate(hp.purchaseDate) }}</span>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as CostAnalysisApi from '@/api/erp/purchase/costAnalysis'
import download from '@/utils/download'

defineOptions({ name: 'ErpPurchaseCostAnalysis' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<CostAnalysisApi.PurchaseCostAnalysisVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderSn: undefined,
  optRealname: undefined,
  lxCreateTime: undefined,
  costReductionMin: undefined,
  costReductionMax: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const detailVisible = ref(false)
const currentRow = ref<CostAnalysisApi.PurchaseCostAnalysisVO | null>(null)
const detailLoading = ref(false)
const detailItems = ref<CostAnalysisApi.OrderItemDetailVO[]>([])
const expandedRows = ref(new Set<number>())
const allExpanded = ref(false)
const previewVisibleKey = ref<string | null>(null)
const previewExpandedKeys = ref(new Set<string>())
let previewHideTimer: ReturnType<typeof setTimeout> | null = null

/** 采购状态映射（领星状态码） */
const PURCHASE_STATUS_MAP: Record<number, { label: string; type: string }> = {
  [-1]: { label: '已作废', type: 'danger' },
  1: { label: '待下单', type: 'info' },
  2: { label: '待签收', type: 'warning' },
  3: { label: '待提交', type: 'info' },
  9: { label: '已完成', type: 'success' },
  121: { label: '待审核', type: 'warning' },
  122: { label: '已驳回', type: 'danger' },
  124: { label: '已作废', type: 'danger' }
}

const getPurchaseStatusLabel = (status?: number) => {
  if (status === undefined || status === null) return '-'
  return PURCHASE_STATUS_MAP[status]?.label ?? `状态(${status})`
}

const getPurchaseStatusType = (status?: number) => {
  if (status === undefined || status === null) return 'info'
  return (PURCHASE_STATUS_MAP[status]?.type as any) ?? 'info'
}

/** 格式化日期（只显示日期部分） */
const formatDate = (val?: Date | string) => {
  if (!val) return '-'
  const d = new Date(val)
  if (isNaN(d.getTime())) return String(val)
  return d.toISOString().slice(0, 10)
}

/** 格式化金额，保留两位小数（用于总价、降本金额等） */
const formatMoney = (val?: number) => {
  if (val === undefined || val === null) return '0.00'
  return Number(val).toFixed(2)
}

/** 格式化单价，保留四位小数（含税单价可能有4位精度） */
const formatPrice = (val?: number) => {
  if (val === undefined || val === null) return '0.0000'
  return Number(val).toFixed(4)
}

const getPreviewKey = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  return `${row.sku || 'sku'}-${index}`
}

const clearPreviewHideTimer = () => {
  if (previewHideTimer) {
    clearTimeout(previewHideTimer)
    previewHideTimer = null
  }
}

const resetImagePreviewState = () => {
  clearPreviewHideTimer()
  previewVisibleKey.value = null
  previewExpandedKeys.value = new Set()
}

const handlePreviewReferenceEnter = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  clearPreviewHideTimer()
  previewVisibleKey.value = getPreviewKey(row, index)
}

const handlePreviewReferenceLeave = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  const key = getPreviewKey(row, index)
  clearPreviewHideTimer()
  previewHideTimer = setTimeout(() => {
    if (previewVisibleKey.value === key) {
      previewVisibleKey.value = null
    }
  }, 120)
}

const handlePreviewPopoverEnter = () => {
  clearPreviewHideTimer()
}

const handlePreviewPopoverLeave = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  const key = getPreviewKey(row, index)
  clearPreviewHideTimer()
  previewHideTimer = setTimeout(() => {
    if (previewVisibleKey.value === key) {
      previewVisibleKey.value = null
    }
  }, 120)
}

const handlePreviewClick = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  const key = getPreviewKey(row, index)
  clearPreviewHideTimer()
  previewExpandedKeys.value = new Set(previewExpandedKeys.value).add(key)
  previewVisibleKey.value = key
}

const isPreviewExpanded = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  return previewExpandedKeys.value.has(getPreviewKey(row, index))
}

const handlePreviewImageClick = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  handlePreviewClick(row, index)
}

const handlePreviewButtonClick = (row: CostAnalysisApi.OrderItemDetailVO, index: number) => {
  handlePreviewClick(row, index)
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CostAnalysisApi.getPurchaseCostAnalysisPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 导出 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await CostAnalysisApi.exportPurchaseCostAnalysis(queryParams)
    download.excel(data, '采购成本统计分析.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 查看统计明细 */
const handleViewDetail = async (row: CostAnalysisApi.PurchaseCostAnalysisVO) => {
  currentRow.value = row
  detailVisible.value = true
  expandedRows.value = new Set()
  allExpanded.value = false
  resetImagePreviewState()
  detailLoading.value = true
  try {
    detailItems.value = await CostAnalysisApi.getOrderItemDetails(row.orderSn)
  } finally {
    detailLoading.value = false
  }
}

/** 切换单行展开 */
const toggleRowExpand = (idx: number) => {
  if (expandedRows.value.has(idx)) {
    expandedRows.value.delete(idx)
  } else {
    expandedRows.value.add(idx)
  }
  expandedRows.value = new Set(expandedRows.value)
}

/** 切换全部展开/折叠 */
const toggleAllExpand = () => {
  allExpanded.value = !allExpanded.value
  if (allExpanded.value) {
    expandedRows.value = new Set(
      detailItems.value
        .map((_, idx) => idx)
        .filter(idx => detailItems.value[idx].historyPrices?.length > 3)
    )
  } else {
    expandedRows.value = new Set()
  }
}

const handleDetailClosed = () => {
  resetImagePreviewState()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
:deep(.purchase-image-popover) {
  padding: 12px;
}

.purchase-image-trigger {
  width: 48px;
  height: 48px;
  margin: 0 auto;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.purchase-image-trigger:hover,
.purchase-image-trigger.is-locked {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px rgb(64 158 255 / 20%);
}

.purchase-image-thumb {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.purchase-image-popover-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.purchase-image-popover-content.is-expanded {
  min-height: 420px;
}

.purchase-image-large {
  width: 296px;
  max-width: 100%;
  max-height: 360px;
  object-fit: contain;
  display: block;
  cursor: zoom-in;
}

.purchase-image-large.is-expanded {
  width: 480px;
  max-height: 560px;
  cursor: default;
}

.purchase-image-actions {
  margin-top: 12px;
}
</style>
