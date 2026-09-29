<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { isAxiosError } from 'axios'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'
import AuthorInfoModal from '@/components/AuthorInfoModal.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 作者信息弹窗：列表接口返回的 AppVO 里已经带了 user，打开弹窗不需要额外请求
const authorModalVisible = ref(false)
const authorUser = ref<API.UserVO>()

const viewAuthor = (user: API.UserVO) => {
  authorUser.value = user
  authorModalVisible.value = true
}

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 优化提示词功能已移除

/**
 * 滚动入场动画。
 *
 * 只做视觉：给 .reveal 的区块加上观察，进入视口时补上 .is-visible 触发 CSS 过渡。
 * 用 IntersectionObserver 而不是监听 scroll，是因为前者由浏览器在合成线程按需回调，
 * 不会因为滚动事件高频触发而掉帧。
 * 不改变任何数据逻辑 —— 区块从一开始就在 DOM 里，只是初始透明、滚到了才显示。
 */
let revealObserver: IntersectionObserver | null = null

const setupReveal = () => {
  const targets = document.querySelectorAll('#homePage .reveal')
  if (!targets.length) {
    return
  }
  // 浏览器不支持时直接全部显示，保证内容永远可见（渐进增强）
  if (typeof IntersectionObserver === 'undefined') {
    targets.forEach((el) => el.classList.add('is-visible'))
    return
  }
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          // 出现过就不用再观察了
          revealObserver?.unobserve(entry.target)
        }
      })
    },
    {
      // 元素露出约 1/8 就开始播动画，滚到底部时不会有"卡住不出现"的感觉
      threshold: 0.12,
      rootMargin: '0px 0px -60px 0px',
    },
  )
  targets.forEach((el) => revealObserver?.observe(el))
}

/**
 * 从异常里取出后端返回的真实错误信息。
 *
 * 后端所有接口都返回 { code, data, message } 包装体，非 0 时 axios 会走 catch，
 * 此时真正的失败原因在 error.response.data.message 里。
 * 如果不把它取出来，用户只会看到笼统的"请重试"，没法判断问题出在哪。
 */
const resolveErrorMessage = (error: unknown): string => {
  if (isAxiosError(error)) {
    const bizMessage = error.response?.data?.message
    if (bizMessage) {
      return bizMessage
    }
    if (error.code === 'ECONNABORTED') {
      return '请求超时，请稍后重试'
    }
    if (!error.response) {
      return '无法连接后端服务，请确认后端已在 8123 端口启动'
    }
    return `请求失败（HTTP ${error.response.status}）`
  }
  return error instanceof Error ? error.message : '未知错误'
}

/**
 * 会话失效时统一处理：清掉本地登录态并引导重新登录。
 * @returns 是否属于"未登录"这一类错误
 */
const handleIfNotLogin = async (error: unknown): Promise<boolean> => {
  if (isAxiosError(error) && error.response?.data?.code === 40100) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.warning('登录状态已失效，请重新登录')
    await router.push(`/user/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    return true
  }
  return false
}

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    // 会话过期（40100）单独处理，其余情况把后端真实原因显示出来
    if (!(await handleIfNotLogin(error))) {
      message.error('创建失败：' + resolveErrorMessage(error))
    }
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
    // 未登录时首页本来就不该有数据，不弹提示；其余错误（后端没起、会话失效等）要说清楚
    if (!isAxiosError(error) || error.response?.data?.code !== 40100) {
      message.error('加载我的应用失败：' + resolveErrorMessage(error))
    }
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
    message.error('加载精选案例失败：' + resolveErrorMessage(error))
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 鼠标跟随光效：把鼠标位置写进 CSS 变量，供背景光晕使用
const handleMouseMove = (e: MouseEvent) => {
  const { clientX, clientY } = e
  const { innerWidth, innerHeight } = window
  document.documentElement.style.setProperty('--mouse-x', `${(clientX / innerWidth) * 100}%`)
  document.documentElement.style.setProperty('--mouse-y', `${(clientY / innerHeight) * 100}%`)
}

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  document.addEventListener('mousemove', handleMouseMove)

  // 等首屏渲染完再观察，否则刚挂载时拿不到 .reveal 元素
  requestAnimationFrame(setupReveal)
})

// 清理：观察器和鼠标监听都要释放，否则路由切走后仍会持有 DOM 引用
onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  revealObserver?.disconnect()
  revealObserver = null
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <p class="hero-eyebrow">峙码 · AI CODE</p>
        <h1 class="hero-title">AI APP GENERATOR</h1>
        <div class="hero-glow" aria-hidden="true"></div>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" class="hero-cta" @click="createApp" :loading="creating">
            立即生成
          </a-button>
        </div>
      </div>

      <!-- 我的作品 -->
      <div class="section reveal">
        <div class="section-head">
          <h2 class="section-title">我的作品</h2>
          <span class="section-more">近期创建</span>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
            @view-author="viewAuthor"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section reveal">
        <div class="section-head">
          <h2 class="section-title">精选案例</h2>
          <span class="section-more">热门推荐</span>
        </div>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
            @view-author="viewAuthor"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
      </div>

      <!-- 作者信息弹窗（数据来自列表里的 AppVO.user，不额外请求接口） -->
      <AuthorInfoModal v-model:open="authorModalVisible" :user="authorUser" />
    </div>

</template>

<style scoped>
/* ==============================================================
   配色严格对齐参考站 https://www.ldmnq.com
     主色 yellow-5  #FFDB18
     悬停/渐变落点  #E27000
     浅黄 #FFF4C5 / 深黄 #E8A600
     底色纯黑，区块面板 #141414，分割线 rgba(255,255,255,.08)
   只改样式，不动任何数据与交互逻辑
   ============================================================== */

#homePage {
  width: 100%;
  margin: 0;
  padding: 0 0 72px;
  min-height: 100vh;
  /* 动态壁纸已移除，改为纯黑 */
  background: #000;
  position: relative;
  overflow: hidden;
}

.container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* ---------------- 首屏 ---------------- */
.hero-section {
  position: relative;
  text-align: center;
  padding: 104px 0 40px;
  color: #fff;
}

/* 标题背后的黄色光晕，营造参考站那种"黑底打光"的质感 */
.hero-glow {
  position: absolute;
  left: 50%;
  top: 46%;
  width: 760px;
  height: 400px;
  transform: translate(-50%, -50%);
  background: radial-gradient(
    ellipse at center,
    rgba(255, 219, 24, 0.2) 0%,
    rgba(255, 219, 24, 0.06) 42%,
    transparent 72%
  );
  filter: blur(8px);
  pointer-events: none;
  z-index: -1;
}

.hero-eyebrow {
  margin: 0 0 18px;
  font-family: var(--ld-display), sans-serif;
  font-size: 13px;
  letter-spacing: 0.34em;
  color: #ffdb18;
  font-weight: 700;
  animation: heroIn 0.8s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* 艺术字标题：白 → 浅黄 → 主黄 → 深黄 的竖向渐变填充，
   再用 drop-shadow 做黄色辉光（对 background-clip:text 的透明文字同样有效） */
.hero-title {
  margin: 0;
  font-size: 68px;
  line-height: 1.1;
  font-weight: 900;
  letter-spacing: 0.01em;
  background: linear-gradient(180deg, #ffffff 0%, #fff4c5 42%, #ffdb18 78%, #e8a600 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 10px 34px rgba(255, 219, 24, 0.28));
  animation: heroIn 0.9s 0.06s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* 标题下方的黄橙光条，强化版式感 */
.hero-section::after {
  content: '';
  display: block;
  width: 92px;
  height: 4px;
  margin: 26px auto 0;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, #ffdb18 30%, #e27000 70%, transparent);
  box-shadow: 0 0 18px rgba(255, 219, 24, 0.55);
  animation: heroIn 0.9s 0.14s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.input-section {
  position: relative;
  margin: 34px auto 0;
  max-width: 820px;
  animation: heroIn 0.9s 0.2s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* 对话框统一白底黑字。
   注意：这里必须把 .ant-input 也写成白底，不能再用 transparent ——
   `.prompt-input.ant-input`（两个类名）优先级高于 `.prompt-input`（一个类名），
   之前那条 transparent 会把上面的白底覆盖掉，结果黑底页面上对话框还是黑的。 */
.prompt-input,
.prompt-input :deep(textarea),
:deep(.prompt-input.ant-input),
:deep(textarea.prompt-input) {
  background: #ffffff !important;
  color: #1a1a1a !important;
}

.prompt-input {
  border-radius: 16px !important;
  border: 1px solid rgba(255, 255, 255, 0.16) !important;
  font-size: 16px;
  padding: 20px 140px 20px 20px !important;
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.55);
  resize: none;
}

:deep(.prompt-input.ant-input),
.prompt-input :deep(textarea) {
  box-shadow: none !important;
  border-color: transparent !important;
}

:deep(.prompt-input.ant-input::placeholder),
:deep(.prompt-input textarea::placeholder) {
  color: #9a9aa0 !important;
}

.prompt-input:focus,
:deep(.prompt-input.ant-input:focus),
:deep(.prompt-input.ant-input-focused) {
  border-color: #ffdb18 !important;
  box-shadow:
    0 0 0 3px rgba(255, 219, 24, 0.28),
    0 22px 60px rgba(0, 0, 0, 0.6) !important;
}

.input-actions {
  position: absolute;
  bottom: 14px;
  right: 14px;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* ---------------- 按钮：参考站 .button-yellow ----------------
   黄底 + 黑字 + 胶囊圆角，悬停时渐变到橙并上浮
   （用两段类名提高优先级，压过 App.vue 里的 .ant-btn-primary） */
.input-actions .hero-cta {
  height: 46px !important;
  min-width: 124px;
  border-radius: 999px !important;
  padding: 0 26px !important;
  font-size: 15px !important;
  font-weight: 700 !important;
  border: 1px solid #ffdb18 !important;
  background: #ffdb18 !important;
  color: #000000 !important;
  box-shadow: 0 10px 24px rgba(255, 219, 24, 0.3);
  transition:
    transform 0.18s cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 0.18s ease,
    background 0.18s ease;
}

.input-actions .hero-cta:not(:disabled):hover {
  background: linear-gradient(135deg, #ffdb18 0%, #e27000 100%) !important;
  border-color: #e27000 !important;
  color: #1a1a1a !important;
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(226, 112, 0, 0.42);
}

.input-actions .hero-cta:not(:disabled):active {
  transform: translateY(0);
}

/* ---------------- 内容区块 ----------------
   背景色取自参考站首屏背景图（strategy-banner-bg.webp）的实测像素值：
     左上角最亮处 #0E356E，沿对角经 #0C2657 / #0B1A3C / #0C101E 渐隐到近黑 #0C0E14；
     右上角另有一处较弱蓝色 #10203E。
   用两层 radial-gradient 还原"顶部两角发蓝、中间到底部近黑"的效果，
   比直接贴图更清晰、也不会把别人的图片放进项目 */
.section {
  margin-top: 44px;
  margin-bottom: 40px;
  padding: 30px 26px 10px;
  border-radius: 20px;
  background:
    radial-gradient(
      125% 105% at 0% 0%,
      #0e356e 0%,
      #0c2657 26%,
      #0b1a3c 52%,
      #0c101e 74%,
      rgba(12, 14, 20, 0) 100%
    ),
    radial-gradient(
      95% 85% at 100% 0%,
      rgba(16, 32, 62, 0.9) 0%,
      rgba(12, 16, 26, 0) 72%
    ),
    #0c0e14;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 26px;
  padding: 0 4px;
}

.section-title {
  position: relative;
  font-size: 28px;
  font-weight: 800;
  margin: 0;
  padding-left: 16px;
  color: #ffffff;
}

/* 标题左侧的黄橙竖条（参考站那种"标题带色块"的版式） */
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 4px;
  height: 22px;
  border-radius: 999px;
  transform: translateY(-50%);
  background: linear-gradient(180deg, #ffdb18, #e27000);
  box-shadow: 0 0 14px rgba(255, 219, 24, 0.5);
}

.section-more {
  font-size: 13px;
  color: #e8a600;
}

.app-grid,
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 12px;
  padding-bottom: 18px;
}

/* ---------------- 滚动入场 ----------------
   初始透明并下移 38px，进入视口后由 JS 补上 .is-visible 触发过渡。
   只影响观感：元素一直在 DOM 里，不影响任何数据与交互。 */
.reveal {
  opacity: 0;
  transform: translateY(38px);
  transition:
    opacity 0.75s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.75s cubic-bezier(0.22, 1, 0.36, 1);
  will-change: opacity, transform;
}

.reveal.is-visible {
  opacity: 1;
  transform: none;
}

@keyframes heroIn {
  from {
    opacity: 0;
    transform: translateY(26px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 64px 0 30px;
  }

  .hero-title {
    font-size: 38px;
  }

  .hero-glow {
    width: 420px;
    height: 260px;
  }

  .prompt-input {
    padding: 18px 18px 66px !important;
  }

  .input-actions {
    right: 12px;
    left: 12px;
    bottom: 12px;
  }

  .hero-cta {
    width: 100%;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .section {
    margin-top: 30px;
    padding: 22px 16px 6px;
    border-radius: 16px;
  }

  .section-title {
    font-size: 22px;
  }
}

/* 尊重系统「减少动态效果」设置：直接显示最终状态，不做位移与淡入 */
@media (prefers-reduced-motion: reduce) {
  .reveal,
  .hero-eyebrow,
  .hero-title,
  .input-section,
  .hero-section::after {
    opacity: 1 !important;
    transform: none !important;
    animation: none !important;
    transition: none !important;
  }
}
</style>
