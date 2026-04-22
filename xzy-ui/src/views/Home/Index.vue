<template>
  <div class="home-container">
    <!-- 顶部欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-left">
        <div class="avatar-wrap">
          <el-avatar :size="72" :src="userStore.getUser.avatar || defaultAvatar" class="user-avatar" />
          <div class="online-dot"></div>
        </div>
        <div class="welcome-text">
          <div class="greeting">{{ greeting }}，<span class="nickname">{{ userStore.getUser.nickname || '用户' }}</span> 👋</div>
          <div class="sub-greeting">{{ mottoText }}</div>
        </div>
      </div>
      <div class="banner-right">
        <div class="datetime-block">
          <div class="current-time">{{ currentTime }}</div>
          <div class="current-date">{{ currentDate }}</div>
          <div class="current-week">{{ currentWeek }}</div>
        </div>
      </div>
    </div>

    <!-- 中间信息卡片区 -->
    <div class="info-grid">
      <!-- 天气卡片 -->
      <div class="card weather-card">
        <div class="card-header">
          <Icon icon="ep:partly-cloudy" class="card-icon" />
          <span>实时天气</span>
        </div>
        <div class="weather-body">
          <div class="weather-main">
            <span class="weather-icon">{{ weatherIcon }}</span>
            <div class="weather-temp-wrap">
              <span class="weather-temp">{{ weather.temp }}°C</span>
              <span class="weather-desc">{{ weather.desc }}</span>
            </div>
          </div>
          <div class="weather-details">
            <div class="weather-detail-item">
              <Icon icon="ep:location" />
              <span>{{ weather.city }}</span>
            </div>
            <div class="weather-detail-item">
              <Icon icon="ep:wind-power" />
              <span>{{ weather.wind }}</span>
            </div>
            <div class="weather-detail-item">
              <Icon icon="ep:cloudy" />
              <span>湿度 {{ weather.humidity }}</span>
            </div>
          </div>
        </div>
        <div v-if="weatherLoading" class="weather-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>获取天气中...</span>
        </div>
      </div>

      <!-- 个人信息卡片 -->
      <div class="card profile-card">
        <div class="card-header">
          <Icon icon="ep:user" class="card-icon" />
          <span>个人信息</span>
        </div>
        <div class="profile-body" v-if="userProfile">
          <div class="profile-item">
            <span class="profile-label"><Icon icon="ep:user-filled" /> 用户名</span>
            <span class="profile-value">{{ userProfile.username }}</span>
          </div>
          <div class="profile-item">
            <span class="profile-label"><Icon icon="ep:phone" /> 手机号</span>
            <span class="profile-value">{{ userProfile.mobile || '未设置' }}</span>
          </div>
          <div class="profile-item">
            <span class="profile-label"><Icon icon="fontisto:email" /> 邮箱</span>
            <span class="profile-value">{{ userProfile.email || '未设置' }}</span>
          </div>
          <div class="profile-item">
            <span class="profile-label"><Icon icon="carbon:tree-view-alt" /> 部门</span>
            <span class="profile-value">{{ userProfile.dept?.name || '未分配' }}</span>
          </div>
          <div class="profile-item">
            <span class="profile-label"><Icon icon="ep:suitcase" /> 岗位</span>
            <span class="profile-value">{{ userProfile.posts?.map((p) => p.name).join('、') || '未分配' }}</span>
          </div>
          <div class="profile-item">
            <span class="profile-label"><Icon icon="ep:key" /> 角色</span>
            <span class="profile-value">{{ userProfile.roles?.map((r) => r.name).join('、') || '无' }}</span>
          </div>
        </div>
        <div v-else class="profile-loading">
          <el-skeleton :rows="4" animated />
        </div>
      </div>

      <!-- 快捷入口卡片 -->
      <div class="card shortcuts-card">
        <div class="card-header">
          <Icon icon="ep:grid" class="card-icon" />
          <span>快捷入口</span>
        </div>
        <div class="shortcuts-grid">
          <div
            v-for="item in shortcuts"
            :key="item.path"
            class="shortcut-item"
            @click="goTo(item.path)"
          >
            <div class="shortcut-icon" :style="{ background: item.color }">
              <Icon :icon="item.icon" />
            </div>
            <span class="shortcut-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录信息 -->
    <div class="login-info" v-if="userProfile">
      <Icon icon="ep:info-filled" class="info-icon" />
      <span>上次登录 IP：{{ userProfile.loginIp || '—' }}
        &nbsp;|&nbsp; 登录时间：{{ formatDate(userProfile.loginDate) || '—' }}
        &nbsp;|&nbsp; 注册时间：{{ formatDate(userProfile.createTime) }}
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import { getUserProfile, ProfileVO } from '@/api/system/user/profile'
import { formatDate } from '@/utils/formatTime'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'Index' })

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const userProfile = ref<ProfileVO | null>(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea0b0de09f75cfdb8ad5aa8c8f839.png'

// ── 时钟 ──────────────────────────────────────────────────────────────────
const now = ref(new Date())
let timer: ReturnType<typeof setInterval>

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
  loadProfile()
  fetchWeather()
})
onUnmounted(() => clearInterval(timer))

const pad = (n: number) => String(n).padStart(2, '0')

const currentTime = computed(() => {
  const d = now.value
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
})

const currentDate = computed(() => {
  const d = now.value
  return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日`
})

const weekMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const currentWeek = computed(() => weekMap[now.value.getDay()])

// ── 问候语 ────────────────────────────────────────────────────────────────
const greeting = computed(() => {
  const h = now.value.getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  if (h < 22) return '晚上好'
  return '夜深了'
})

const mottoList = [
  '今日事今日毕，保持专注，高效前行！',
  '每一个努力的今天，都是明天的财富。',
  '成功不是偶然，坚持才是关键。',
  '把每一件平凡的事做好，就是不平凡。',
  '不积跬步，无以至千里。'
]
const mottoText = mottoList[new Date().getDay() % mottoList.length]

// ── 天气 ──────────────────────────────────────────────────────────────────
const weatherLoading = ref(true)
const weather = ref({
  city: '获取中...',
  temp: '--',
  desc: '--',
  wind: '--',
  humidity: '--'
})

const weatherIconMap: Record<string, string> = {
  晴: '☀️', 多云: '⛅', 阴: '☁️', 小雨: '🌧️', 中雨: '🌧️',
  大雨: '⛈️', 暴雨: '🌩️', 雪: '❄️', 雾: '🌫️', 霾: '🌫️'
}
const weatherIcon = computed(() => {
  for (const k of Object.keys(weatherIconMap)) {
    if (weather.value.desc.includes(k)) return weatherIconMap[k]
  }
  return '🌤️'
})

const fetchWeather = async () => {
  try {
    // wttr.in 免费天气接口，format=j1 返回 JSON
    const res = await fetch('https://wttr.in/Shenzhen?format=j1&lang=zh')
    const data = await res.json()
    const cur = data?.current_condition?.[0]
    weather.value = {
      city: '深圳',
      temp: cur?.temp_C ?? '--',
      desc: cur?.weatherDesc?.[0]?.value ?? '--',
      wind: `${cur?.winddir16Point ?? ''} ${cur?.windspeedKmph ?? '--'} km/h`,
      humidity: cur?.humidity ? `${cur.humidity}%` : '--'
    }
  } catch {
    weather.value = { city: '未知', temp: '--', desc: '获取失败', wind: '--', humidity: '--' }
  } finally {
    weatherLoading.value = false
  }
}

// ── 个人资料 ──────────────────────────────────────────────────────────────
const loadProfile = async () => {
  try {
    userProfile.value = await getUserProfile()
  } catch {}
}

// ── 快捷入口（按权限过滤） ──────────────────────────────────────────────
const allShortcuts = [
  { label: '个人中心', icon: 'ep:user', path: '/user/profile', color: 'linear-gradient(135deg,#667eea,#764ba2)' },
  { label: '系统用户', icon: 'ep:avatar', path: '/system/user', color: 'linear-gradient(135deg,#f093fb,#f5576c)' },
  { label: '角色管理', icon: 'ep:key', path: '/system/role', color: 'linear-gradient(135deg,#4facfe,#00f2fe)' },
  { label: '菜单管理', icon: 'ep:menu', path: '/system/menu', color: 'linear-gradient(135deg,#43e97b,#38f9d7)' },
  { label: '操作日志', icon: 'ep:document', path: '/system/operate-log', color: 'linear-gradient(135deg,#fa709a,#fee140)' },
  { label: '字典管理', icon: 'ep:collection', path: '/system/dict', color: 'linear-gradient(135deg,#a18cd1,#fbc2eb)' }
]

// 递归收集所有已授权的路径
const collectPaths = (routes: AppRouteRecordRaw[]): Set<string> => {
  const paths = new Set<string>()
  const walk = (list: AppRouteRecordRaw[]) => {
    for (const r of list) {
      if (r.path) paths.add(r.path)
      if (r.children?.length) walk(r.children as AppRouteRecordRaw[])
    }
  }
  walk(routes)
  return paths
}

const shortcuts = computed(() => {
  const authorizedPaths = collectPaths(permissionStore.getRouters)
  // 个人中心不走动态路由，始终显示
  return allShortcuts.filter(
    (s) => s.path === '/user/profile' || authorizedPaths.has(s.path)
  )
})

const goTo = (path: string) => router.push(path)

</script>

<style scoped lang="scss">
.home-container {
  padding: 20px;
  min-height: calc(100vh - 120px);
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ── 欢迎横幅 ── */
.welcome-banner {
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
  border-radius: 16px;
  padding: 28px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.35);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -40px;
    right: -40px;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.06);
    border-radius: 50%;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: -60px;
    left: 30%;
    width: 260px;
    height: 260px;
    background: rgba(255, 255, 255, 0.04);
    border-radius: 50%;
  }
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 1;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;

  .user-avatar {
    border: 3px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }

  .online-dot {
    position: absolute;
    bottom: 4px;
    right: 4px;
    width: 14px;
    height: 14px;
    background: #52c41a;
    border-radius: 50%;
    border: 2px solid #fff;
  }
}

.welcome-text {
  .greeting {
    font-size: 22px;
    font-weight: 700;
    line-height: 1.3;

    .nickname {
      color: #ffd54f;
    }
  }
  .sub-greeting {
    margin-top: 6px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.75);
  }
}

.banner-right {
  z-index: 1;
  text-align: right;

  .current-time {
    font-size: 42px;
    font-weight: 700;
    letter-spacing: 2px;
    line-height: 1;
    font-variant-numeric: tabular-nums;
  }
  .current-date {
    margin-top: 6px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
  }
  .current-week {
    margin-top: 2px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
  }
}

/* ── 信息卡片区 ── */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1.4fr 1fr;
  gap: 20px;
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 22px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.25s;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;

  .card-icon {
    font-size: 18px;
    color: #1a73e8;
  }
}

/* 天气卡片 */
.weather-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 14px;

  .weather-icon {
    font-size: 48px;
    line-height: 1;
  }

  .weather-temp-wrap {
    display: flex;
    flex-direction: column;

    .weather-temp {
      font-size: 36px;
      font-weight: 700;
      color: #1a73e8;
      line-height: 1;
    }
    .weather-desc {
      margin-top: 4px;
      font-size: 14px;
      color: #666;
    }
  }
}

.weather-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weather-detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.weather-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 13px;
  margin-top: 12px;
}

/* 个人信息卡片 */
.profile-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 10px;
  border-radius: 8px;
  background: #fafafa;
  font-size: 13px;

  .profile-label {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #888;
    flex-shrink: 0;
    width: 80px;
  }

  .profile-value {
    color: #333;
    font-weight: 500;
    text-align: right;
    word-break: break-all;
  }
}

/* 快捷入口 */
.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 12px 6px;
  border-radius: 10px;
  transition: background 0.2s, transform 0.15s;

  &:hover {
    background: #f5f7ff;
    transform: translateY(-2px);
  }

  .shortcut-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #fff;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  }

  .shortcut-label {
    font-size: 12px;
    color: #555;
    text-align: center;
    white-space: nowrap;
  }
}

/* 登录信息栏 */
.login-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
  background: #fff;
  border-radius: 10px;
  padding: 12px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .info-icon {
    color: #1a73e8;
    font-size: 14px;
    flex-shrink: 0;
  }
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }
  .shortcuts-card {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 12px;
  }
  .welcome-banner {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 20px;

    .banner-right {
      text-align: center;
    }
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
