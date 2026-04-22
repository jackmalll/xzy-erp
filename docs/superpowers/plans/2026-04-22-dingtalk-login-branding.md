# DingTalk Login Branding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在钉钉免登页面顶部增加独立品牌区，展示项目现有 logo 和蓝色加粗标题 `统一报表平台`，同时保持当前登录成功/失败逻辑不变。

**Architecture:** 仅修改 `xzy-ui/src/views/Login/components/DingTalkLogin.vue`。在现有状态卡片外层增加品牌区模板，复用项目已有 `@/assets/imgs/logo.png` 资源，并补充与品牌区对应的样式，使页面形成“品牌区在上、状态卡片在下”的纵向布局。

**Tech Stack:** Vue 3、`<script setup lang="ts">`、Element Plus、SCSS、Vite 资源导入

---

### Task 1: 调整钉钉免登页模板与资源引用

**Files:**
- Modify: `xzy-ui/src/views/Login/components/DingTalkLogin.vue`

- [ ] **Step 1: 在脚本中引入 logo 资源并准备模板使用变量**

```ts
import logoImg from '@/assets/imgs/logo.png'
```

将其加入 `DingTalkLogin.vue` 现有 imports 区域，保持其他登录逻辑不变。

- [ ] **Step 2: 在模板中于状态卡片上方增加品牌区**

```vue
<template>
  <div class="dingtalk-login-container">
    <div class="dingtalk-login-content">
      <div class="brand-section">
        <img :src="logoImg" alt="统一报表平台" class="brand-logo" />
        <h1 class="brand-title">统一报表平台</h1>
      </div>
      <div class="status-card">
        <!-- 保持现有 loading / error 结构 -->
      </div>
    </div>
  </div>
</template>
```

要求：品牌区必须位于状态卡片之前，状态卡片内部现有加载和错误展示结构保持不变。

- [ ] **Step 3: 补充布局与品牌样式**

```scss
.dingtalk-login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
}

.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.brand-logo {
  width: 240px;
  max-width: 60vw;
  object-fit: contain;
}

.brand-title {
  margin: 0;
  color: #2f95d1;
  font-size: 30px;
  font-weight: 700;
  font-family: 'SimHei', 'Microsoft YaHei', sans-serif;
  line-height: 1.2;
}
```

要求：保留现有 `.status-card` 样式，不修改登录状态文本和错误逻辑。

- [ ] **Step 4: 检查页面结构无多余代码**

确认组件中不存在未使用的导入、未使用变量，且未重新引入“返回登录页”按钮。

- [ ] **Step 5: 提交本任务改动**

```bash
git add xzy-ui/src/views/Login/components/DingTalkLogin.vue
git commit -m "feat: add dingtalk login branding"
```

### Task 2: 手工验证页面展示与登录流程

**Files:**
- Modify: `xzy-ui/src/views/Login/components/DingTalkLogin.vue`

- [ ] **Step 1: 启动前端开发服务**

```bash
pnpm dev
```

在 `d:\projectcode\ruoyi-vue-pro-new\xzy-ui` 目录运行，预期输出包含 Vite 本地访问地址。

- [ ] **Step 2: 访问钉钉免登页并确认视觉结果**

检查以下结果：

```text
1. 页面顶部显示 logo
2. logo 下方显示“统一报表平台”
3. 标题为蓝色且明显加粗
4. 品牌区与状态卡片垂直分离
```

- [ ] **Step 3: 分别验证成功与失败路径不受影响**

```text
1. 登录成功时仍然进入系统首页
2. 登录失败时仍然展示错误信息
3. 页面不出现普通登录页回退按钮
```

- [ ] **Step 4: 提交验证后的最终改动**

```bash
git add xzy-ui/src/views/Login/components/DingTalkLogin.vue
git commit -m "test: verify dingtalk login branding"
```

## Self-Review
- Spec coverage：已覆盖品牌区位置、logo 复用、标题文案/字体/颜色、成功失败逻辑保持不变。
- Placeholder scan：无 `TODO/TBD` 类占位描述。
- Type consistency：模板、脚本、样式中的 `logoImg`、`brand-section`、`brand-logo`、`brand-title` 命名一致。
