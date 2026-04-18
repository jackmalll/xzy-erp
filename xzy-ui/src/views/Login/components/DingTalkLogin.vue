<template>
  <div class="dingtalk-login-container">
    <div class="status-card">
      <div v-if="loading" class="status-inner">
        <div class="status-top">
          <span class="spinner"></span>
          <span class="status-text">正在通过钉钉登录</span>
        </div>
        <p class="status-desc">授权成功后将自动进入统一报表平台</p>
      </div>
      <div v-else-if="error" class="status-inner">
        <el-icon color="#f56c6c" :size="36">
          <CircleClose />
        </el-icon>
        <p class="error-message">{{ error }}</p>
        <el-button type="primary" @click="handleRetry">返回登录页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dingTalkLogin } from '@/api/login'
import { useUserStore } from '@/store/modules/user'
import { deleteUserCache } from '@/hooks/web/useCache'
import * as authUtil from '@/utils/auth'
import * as dd from 'dingtalk-jsapi'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const error = ref('')

// 钉钉企业 corpId（从环境变量或配置中读取）
const DINGTALK_CORP_ID = import.meta.env.VITE_DINGTALK_CORP_ID || 'your_corp_id'

const isDingTalkEnv = () => {
  return navigator.userAgent.toLowerCase().includes('dingtalk')
}

type DingTalkAuthCodeResult = {
  code: string
}

type DingTalkError = {
  errorMessage?: string
  message?: string
}

onMounted(() => {
  authUtil.removeToken()
  deleteUserCache()
  userStore.resetState()

  if (!isDingTalkEnv()) {
    error.value = '请在钉钉客户端中打开'
    loading.value = false
    return
  }

  let ready = false
  let isLoggingIn = false
  const readyTimeout = window.setTimeout(() => {
    if (ready) {
      return
    }
    loading.value = false
    error.value = '钉钉环境初始化失败，请在钉钉客户端内重新打开应用'
    ElMessage.error(error.value)
  }, 3000)

  dd.ready(() => {
    ready = true
    window.clearTimeout(readyTimeout)
    ;(dd.runtime.permission.requestAuthCode({
      corpId: DINGTALK_CORP_ID
    }) as Promise<DingTalkAuthCodeResult>)
      .then(async (result) => {
        if (isLoggingIn) return
        isLoggingIn = true
        try {
          const res = await dingTalkLogin({ authCode: result.code })

          authUtil.setToken(res)

          ElMessage.success('登录成功')

          await router.replace('/')
        } catch (err) {
          isLoggingIn = false
          loading.value = false
          error.value = (err as DingTalkError).message || '登录失败，请重试'
          ElMessage.error(error.value)
        }
      })
      .catch((err: DingTalkError) => {
        loading.value = false
        error.value = `获取授权码失败: ${err.errorMessage || err.message || '未知错误'}`
        ElMessage.error(error.value)
      })
  })

  dd.error((err: DingTalkError) => {
    window.clearTimeout(readyTimeout)
    loading.value = false
    error.value = `钉钉 JSAPI 错误: ${err.errorMessage || '未知错误'}`
    ElMessage.error(error.value)
  })
})

const handleRetry = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.dingtalk-login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #ffffff;
}

.status-card {
  width: 360px;
  background: #ffffff;
  border: 1px solid #e4e7ec;
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.08);
}

.status-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}

.status-top {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1677ff;
  font-size: 16px;
  font-weight: 600;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #d0d5dd;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.status-text {
  color: #1677ff;
}

.status-desc {
  margin: 0;
  font-size: 13px;
  color: #667085;
  line-height: 1.8;
}

.error-message {
  color: #f56c6c;
  margin: 0;
  font-size: 14px;
}
</style>
