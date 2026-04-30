<template>
  <ContentWrap>
    <div class="mb-24px">
      <h2 class="text-20px font-bold text-gray-800 m-0 mb-4px">基准单价（N）配置</h2>
    </div>

    <el-form ref="formRef" :model="form" v-loading="loading" label-width="0">

      <!-- 规则配置 -->
      <div class="config-section">
        <div class="section-header">
          <span class="section-title">规则配置</span>
          <span class="section-tip">勾选并保存后生效</span>
        </div>

        <!-- 最小值规则 -->
        <div class="rule-row">
          <el-checkbox v-model="form.ruleMinPrice" @change="handleMinPriceChange">
            N = SKU所有历史采购单价中的<strong>最小值</strong>
            （仅"有效采购单价"参与计算，风控配置中无效单价和审核未通过单价不参与计算）
          </el-checkbox>
        </div>

        <!-- 三段式规则统一勾选 -->
        <div class="rule-row rule-inline">
          <el-checkbox v-model="form.ruleThreeEnabled" @change="handleThreeRuleChange">
            启用三段式规则（勾选后以下三条规则同时生效）
          </el-checkbox>
          <template v-if="form.ruleThreeEnabled">
            <span class="ml-16px">计算方式：</span>
            <el-select
              v-model="form.ruleThreeCalcType"
              class="calc-type-select"
              size="small"
            >
              <el-option label="均值" value="avg" />
              <el-option label="最小值" value="min" />
            </el-select>
          </template>
        </div>

        <div class="three-rules-wrap" :class="{ 'three-rules-disabled': !form.ruleThreeEnabled }">
          <!-- 规则1 -->
          <div class="rule-row rule-inline">
            <span class="rule-index">规则1</span>
            <span>若历史采购次数</span>
            <span class="operator"> ≤ </span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule1MaxCount"
                :disabled="!form.ruleThreeEnabled"
                :min="1"
                :max="999"
                :class="['num-input', { 'input-error': errors.rule1MaxCount }]"
                controls-position="right"
                @change="clearError('rule1MaxCount')"
              />
              <span v-if="errors.rule1MaxCount" class="error-tip">不能为空</span>
            </div>
            <span>，则 N = 第</span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule1AvgFrom"
                :disabled="!form.ruleThreeEnabled"
                :min="1"
                :max="form.rule1MaxCount ?? 999"
                :class="['num-input', { 'input-error': errors.rule1AvgFrom }]"
                controls-position="right"
                @change="clearError('rule1AvgFrom')"
              />
              <span v-if="errors.rule1AvgFrom" class="error-tip">不能为空</span>
            </div>
            <span>次~第</span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule1AvgTo"
                :disabled="!form.ruleThreeEnabled"
                :min="form.rule1AvgFrom ?? 1"
                :max="form.rule1MaxCount ?? 999"
                :class="['num-input', { 'input-error': errors.rule1AvgTo }]"
                controls-position="right"
                @change="clearError('rule1AvgTo')"
              />
              <span v-if="errors.rule1AvgTo" class="error-tip">不能为空</span>
            </div>
            <span>次采购单价的<strong>{{ form.ruleThreeCalcType === 'min' ? '最小值' : '均值' }}</strong></span>
          </div>

          <!-- 规则2 -->
          <div class="rule-row rule-inline">
            <span class="rule-index">规则2</span>
            <span>若历史采购次数</span>
            <span class="operator"> &gt; </span>
            <span class="num-input-readonly">{{ form.rule1MaxCount ?? '-' }}</span>
            <span>且</span>
            <span class="operator"> &lt; </span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule2MaxCount"
                :disabled="!form.ruleThreeEnabled"
                :min="(form.rule1MaxCount ?? 0) + 1"
                :max="999"
                :class="['num-input', { 'input-error': errors.rule2MaxCount }]"
                controls-position="right"
                @change="clearError('rule2MaxCount')"
              />
              <span v-if="errors.rule2MaxCount" class="error-tip">不能为空</span>
            </div>
            <span>，则 N = 第</span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule2AvgFrom"
                :disabled="!form.ruleThreeEnabled"
                :min="(form.rule1MaxCount ?? 0) + 1"
                :max="form.rule2MaxCount != null ? form.rule2MaxCount - 1 : 999"
                :class="['num-input', { 'input-error': errors.rule2AvgFrom }]"
                controls-position="right"
                @change="clearError('rule2AvgFrom')"
              />
              <span v-if="errors.rule2AvgFrom" class="error-tip">不能为空</span>
            </div>
            <span>次~第</span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule2AvgTo"
                :disabled="!form.ruleThreeEnabled"
                :min="form.rule2AvgFrom ?? (form.rule1MaxCount ?? 0) + 1"
                :max="form.rule2MaxCount != null ? form.rule2MaxCount - 1 : 999"
                :class="['num-input', { 'input-error': errors.rule2AvgTo }]"
                controls-position="right"
                @change="clearError('rule2AvgTo')"
              />
              <span v-if="errors.rule2AvgTo" class="error-tip">不能为空</span>
            </div>
            <span>次采购单价的<strong>{{ form.ruleThreeCalcType === 'min' ? '最小值' : '均值' }}</strong></span>
          </div>

          <!-- 规则3 -->
          <div class="rule-row rule-inline">
            <span class="rule-index">规则3</span>
            <span>若历史采购次数</span>
            <span class="operator"> ≥ </span>
            <span class="num-input-readonly">{{ form.rule2MaxCount ?? '-' }}</span>
            <span>，则 N = 第</span>
            <div class="input-with-error">
              <el-input-number
                v-model="form.rule3AvgFrom"
                :disabled="!form.ruleThreeEnabled"
                :min="form.rule2MaxCount ?? 1"
                :max="999"
                :class="['num-input', { 'input-error': errors.rule3AvgFrom }]"
                controls-position="right"
                @change="clearError('rule3AvgFrom')"
              />
              <span v-if="errors.rule3AvgFrom" class="error-tip">不能为空</span>
            </div>
            <span>次~</span>
            <template v-if="!form.rule3AvgToLatest">
              <div class="input-with-error">
                <el-input-number
                  v-model="form.rule3AvgTo"
                  :disabled="!form.ruleThreeEnabled"
                  :min="form.rule3AvgFrom ?? form.rule2MaxCount ?? 1"
                  :max="999"
                  :class="['num-input', { 'input-error': errors.rule3AvgTo }]"
                  controls-position="right"
                  @change="clearError('rule3AvgTo')"
                />
                <span v-if="errors.rule3AvgTo" class="error-tip">不能为空</span>
              </div>
              <span>次</span>
            </template>
            <template v-else>
              <span class="tag-latest">最新一次</span>
            </template>
            <el-checkbox
              v-model="form.rule3AvgToLatest"
              :disabled="!form.ruleThreeEnabled"
              class="ml-8px"
            >取到最新一次</el-checkbox>
            <span>采购单价的<strong>{{ form.ruleThreeCalcType === 'min' ? '最小值' : '均值' }}</strong></span>
          </div>
        </div>
      </div>

      <!-- 风控配置 -->
      <div class="config-section mt-32px">
        <div class="section-header">
          <span class="section-title">风控配置</span>
        </div>
        <div class="section-tip-desc">以下规则勾选生效（可多选）</div>

        <!-- 风控1：SKU数量 -->
        <div class="rule-row rule-inline">
          <el-checkbox v-model="form.riskQtyEnabled" class="rule-checkbox" />
          <span>若采购SKU数量</span>
          <span class="operator"> &lt; </span>
          <el-input-number
            v-model="form.riskQtyThreshold"
            :min="1"
            :max="99999"
            class="num-input"
            controls-position="right"
            placeholder="数量"
          />
          <span>，则本次采购该SKU的采购单价不参与统计，标记为</span>
          <span class="tag-invalid">"无效|忽略"</span>
          <span>。</span>
        </div>

        <!-- 风控2：SKU总金额 -->
        <div class="rule-row rule-inline">
          <el-checkbox v-model="form.riskAmountEnabled" class="rule-checkbox" />
          <span>若采购SKU总金额</span>
          <span class="operator"> &lt; </span>
          <el-input-number
            v-model="form.riskAmountThreshold"
            :min="0"
            :precision="2"
            class="num-input num-input-wide"
            controls-position="right"
            placeholder="金额"
          />
          <span>，则本次采购该SKU的采购单价不参与统计，标记为</span>
          <span class="tag-invalid">"无效|忽略"</span>
          <span>。</span>
        </div>

        <!-- 风控3：单价浮动 -->
        <div class="rule-row rule-inline">
          <el-checkbox v-model="form.riskPriceFloatEnabled" class="rule-checkbox" />
          <span>若本次采购SKU单价浮动（上升/下降）</span>
          <span class="operator"> &gt; </span>
          <el-input-number
            v-model="form.riskPriceFloatPercent"
            :min="0"
            :max="9999"
            :precision="2"
            class="num-input"
            controls-position="right"
            placeholder="百分比"
          />
          <span>%，则本次采购单价标记为</span>
          <span class="tag-pending">"待审核"</span>
          <span>。</span>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="mt-32px">
        <el-button type="primary" :loading="saving" @click="handleSave">
          <Icon icon="ep:check" class="mr-4px" />
          保存配置
        </el-button>
        <el-button @click="handleReset">
          <Icon icon="ep:refresh" class="mr-4px" />
          重置
        </el-button>
      </div>
    </el-form>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as BasePriceConfigApi from '@/api/erp/purchase/basePriceConfig'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'ErpBasePriceConfig' })

interface FormModel {
  ruleMinPrice: boolean
  ruleThreeEnabled: boolean
  ruleThreeCalcType: 'avg' | 'min'
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
  rule3AvgToLatest: boolean
  riskQtyEnabled: boolean
  riskQtyThreshold: number | null
  riskAmountEnabled: boolean
  riskAmountThreshold: number | null
  riskPriceFloatEnabled: boolean
  riskPriceFloatPercent: number | null
}

const defaultForm = (): FormModel => ({
  ruleMinPrice: false,
  ruleThreeEnabled: false,
  ruleThreeCalcType: 'avg',
  rule1MaxCount: 3,
  rule1AvgFrom: 1,
  rule1AvgTo: 3,
  rule2MinCount: 3,
  rule2MaxCount: 10,
  rule2AvgFrom: 5,
  rule2AvgTo: 8,
  rule3MinCount: 10,
  rule3AvgFrom: 5,
  rule3AvgTo: 8,
  rule3AvgToLatest: false,
  riskQtyEnabled: false,
  riskQtyThreshold: null,
  riskAmountEnabled: false,
  riskAmountThreshold: null,
  riskPriceFloatEnabled: false,
  riskPriceFloatPercent: null
})

const message = useMessage()

const loading = ref(false)
const saving = ref(false)
const originalForm = ref<FormModel>(defaultForm())
const threeRuleFields = [
  'rule1MaxCount',
  'rule1AvgFrom',
  'rule1AvgTo',
  'rule2MinCount',
  'rule2MaxCount',
  'rule2AvgFrom',
  'rule2AvgTo',
  'rule3MinCount',
  'rule3AvgFrom',
  'rule3AvgTo'
] as const

const form = ref<FormModel>(defaultForm())

const buildSavePayload = (): FormModel => {
  const payload = { ...form.value }
  if (payload.ruleThreeEnabled) {
    payload.rule2MinCount = payload.rule1MaxCount
    payload.rule3MinCount = payload.rule2MaxCount
    if (payload.rule3AvgToLatest) {
      payload.rule3AvgTo = -1
    }
  } else {
    threeRuleFields.forEach((field) => {
      payload[field] = null
    })
  }
  return payload
}

const loadConfig = async () => {
  loading.value = true
  try {
    const data = await BasePriceConfigApi.getBasePriceConfig()
    if (data) {
      const rule3AvgToLatest = data.rule3AvgTo === -1
      form.value = {
        ...defaultForm(),
        ...data,
        ruleMinPrice: data.ruleMinPrice ?? false,
        ruleThreeEnabled: data.ruleThreeEnabled ?? false,
        riskQtyEnabled: data.riskQtyEnabled ?? false,
        riskAmountEnabled: data.riskAmountEnabled ?? false,
        riskPriceFloatEnabled: data.riskPriceFloatEnabled ?? false,
        ruleThreeCalcType: (data.ruleThreeCalcType as 'avg' | 'min') ?? 'avg',
        rule3AvgToLatest,
        rule3AvgTo: rule3AvgToLatest ? null : (data.rule3AvgTo ?? null)
      }
      originalForm.value = { ...form.value }
    }
  } finally {
    loading.value = false
  }
}

type ThreeRuleField = 'rule1MaxCount' | 'rule1AvgFrom' | 'rule1AvgTo' | 'rule2MaxCount' | 'rule2AvgFrom' | 'rule2AvgTo' | 'rule3AvgFrom' | 'rule3AvgTo'

const errors = ref<Partial<Record<ThreeRuleField, boolean>>>({})

const clearError = (field: ThreeRuleField) => {
  errors.value[field] = false
}

const validateThreeRules = (): boolean => {
  if (!form.value.ruleThreeEnabled) return true
  const fields: ThreeRuleField[] = [
    'rule1MaxCount', 'rule1AvgFrom', 'rule1AvgTo',
    'rule2MaxCount', 'rule2AvgFrom', 'rule2AvgTo',
    'rule3AvgFrom',
    ...(form.value.rule3AvgToLatest ? [] : ['rule3AvgTo' as ThreeRuleField])
  ]
  let valid = true
  fields.forEach((field) => {
    if (form.value[field] == null) {
      errors.value[field] = true
      valid = false
    } else {
      errors.value[field] = false
    }
  })
  return valid
}

const handleSave = async () => {
  if (!validateThreeRules()) return
  saving.value = true
  try {
    const payload = buildSavePayload()
    await BasePriceConfigApi.saveBasePriceConfig(payload)
    form.value = { ...payload }
    originalForm.value = { ...payload }
    message.success('保存成功')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  if (originalForm.value) {
    form.value = { ...originalForm.value }
  }
}

const handleMinPriceChange = (val: boolean) => {
  if (val) {
    form.value.ruleThreeEnabled = false
  }
}

const handleThreeRuleChange = (val: boolean) => {
  if (val) {
    form.value.ruleMinPrice = false
  }
}

watch(
  () => form.value.rule1MaxCount,
  (val) => {
    if (val == null) return
    const maxAvg = val
    if ((form.value.rule1AvgFrom ?? 0) > maxAvg) form.value.rule1AvgFrom = maxAvg
    if ((form.value.rule1AvgTo ?? 0) > maxAvg) form.value.rule1AvgTo = maxAvg
    const minRule2 = val + 1
    if ((form.value.rule2MaxCount ?? 0) < minRule2) form.value.rule2MaxCount = minRule2
    if ((form.value.rule2AvgFrom ?? 0) < minRule2) form.value.rule2AvgFrom = minRule2
    if ((form.value.rule2AvgTo ?? 0) < minRule2) form.value.rule2AvgTo = minRule2
  }
)

watch(
  () => form.value.rule2MaxCount,
  (val) => {
    if (val == null) return
    const maxAvg2 = val - 1
    if ((form.value.rule2AvgFrom ?? 0) > maxAvg2) form.value.rule2AvgFrom = maxAvg2
    if ((form.value.rule2AvgTo ?? 0) > maxAvg2) form.value.rule2AvgTo = maxAvg2
    if ((form.value.rule3AvgFrom ?? 0) < val) form.value.rule3AvgFrom = val
    if ((form.value.rule3AvgTo ?? 0) < val) form.value.rule3AvgTo = val
  }
)

watch(
  () => form.value.rule1AvgFrom,
  (val) => {
    if (val == null) return
    if ((form.value.rule1AvgTo ?? 0) < val) form.value.rule1AvgTo = val
  }
)

watch(
  () => form.value.rule2AvgFrom,
  (val) => {
    if (val == null) return
    if ((form.value.rule2AvgTo ?? 0) < val) form.value.rule2AvgTo = val
  }
)

watch(
  () => form.value.rule3AvgFrom,
  (val) => {
    if (val == null) return
    if ((form.value.rule3AvgTo ?? 0) < val) form.value.rule3AvgTo = val
  }
)

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.config-section {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 20px 24px;
  background: #fafafa;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.section-tip {
  font-size: 13px;
  color: #909399;
}

.section-tip-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 14px;
}

.section-sub-tip {
  font-size: 13px;
  color: #909399;
  margin: 12px 0 8px 0;
}

.rule-row {
  margin-bottom: 12px;
  line-height: 32px;
}

.rule-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 14px;
  color: #303133;
}

.rule-checkbox {
  margin-right: 4px;
}

.operator {
  font-weight: 600;
  color: #409eff;
  padding: 0 2px;
}

.num-input {
  width: 80px;
}

.num-input-wide {
  width: 120px;
}

.tag-invalid {
  color: #e6a23c;
  font-weight: 500;
}

.tag-pending {
  color: #f56c6c;
  font-weight: 500;
}

.three-rules-wrap {
  margin-top: 10px;
  padding: 12px 16px;
  border: 1px dashed #c0c4cc;
  border-radius: 4px;
  background: #fff;
}

.three-rules-disabled {
  opacity: 0.6;
}

.input-with-error {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
  vertical-align: top;
}

.error-tip {
  font-size: 12px;
  color: #f56c6c;
  line-height: 1.2;
  margin-top: 2px;
}

.input-error :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset !important;
}

.tag-latest {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f0f9eb;
  color: #67c23a;
  font-size: 13px;
  font-weight: 500;
}

.ml-8px {
  margin-left: 8px;
}

.ml-16px {
  margin-left: 16px;
}

.calc-type-select {
  width: 90px;
}

.num-input-readonly {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 80px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.rule-index {
  display: inline-block;
  min-width: 44px;
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  border-radius: 3px;
  padding: 0 6px;
  line-height: 22px;
  text-align: center;
  margin-right: 2px;
}
</style>
