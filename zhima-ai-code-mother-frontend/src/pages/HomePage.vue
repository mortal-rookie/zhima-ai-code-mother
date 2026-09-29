<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { motionSurface as vMotionSurface } from '@/utils/motion'
import { useRouter } from 'vue-router'
import {
  AppstoreOutlined,
  LayoutOutlined,
  BarChartOutlined,
  StarOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons-vue'
import AtmosphereArt from '@/components/AtmosphereArt.vue'
import KleeCompanion from '@/components/KleeCompanion.vue'
import PromptComposer from '@/components/PromptComposer.vue'
import AppCard from '@/components/AppCard.vue'
import AuthorInfoModal from '@/components/AuthorInfoModal.vue'
import { listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getDeployUrl } from '@/config/env'
const router = useRouter()
const store = useLoginUserStore()
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const loading = ref(false)
const listError = ref(false)
let listRequest = 0
const author = ref<API.UserVO>()
const authorVisible = ref(false)
const shortcuts = [
  { name: '从模板创建', desc: '从灵感开始', icon: AppstoreOutlined, path: '/templates' },
  { name: '网页应用', desc: '网站 / 管理系统', icon: LayoutOutlined, path: '/app/new' },
  {
    name: '数据应用',
    desc: '可视化 / 数据看板',
    icon: BarChartOutlined,
    path: '/app/new?template=dashboard',
  },
  {
    name: 'AI 应用',
    desc: '助手界面 / 交互原型',
    icon: StarOutlined,
    path: '/app/new?template=assistant',
  },
]
async function loadApps() {
  const requestId = ++listRequest
  if (!store.loginUser.id) myApps.value = []
  loading.value = true
  listError.value = false
  const query = { pageNum: 1, pageSize: 6, sortField: 'createTime', sortOrder: 'descend' }
  const results = await Promise.allSettled([
    listGoodAppVoByPage(query),
    store.loginUser.id ? listMyAppVoByPage(query) : Promise.resolve(null),
  ])
  if (requestId !== listRequest) return
  const featured = results[0]
  const mine = results[1]
  if (featured?.status === 'fulfilled' && featured.value?.data.code === 0)
    featuredApps.value = featured.value.data.data?.records || []
  else listError.value = true
  if (mine?.status === 'fulfilled' && mine.value?.data.code === 0)
    myApps.value = mine.value.data.data?.records || []
  else if (mine?.status === 'rejected') listError.value = true
  loading.value = false
}
function viewChat(id: string | number | undefined) {
  if (id) router.push(`/app/chat/${String(id)}?view=1`)
}
function viewWork(app: API.AppVO) {
  if (app.deployKey) window.open(getDeployUrl(app.deployKey), '_blank', 'noopener,noreferrer')
}
function viewAuthor(user: API.UserVO) {
  author.value = user
  authorVisible.value = true
}
watch(() => store.loginUser.id, loadApps)
onMounted(loadApps)
</script>
<template>
  <div class="home-page">
    <section v-motion-surface class="home-hero">
      <AtmosphereArt />
      <div class="home-inner">
        <div class="home-intro">
          <div>
            <div class="welcome-label">你的创作空间</div>
            <h1 class="display-title">你好，<br />今天想创造什么？</h1>
          </div>
          <KleeCompanion />
        </div>
        <PromptComposer />
        <div class="shortcut-grid">
          <RouterLink
            v-for="item in shortcuts"
            :key="item.name"
            :to="item.path"
            v-motion-surface
            class="shortcut"
            ><span class="shortcut-icon"><component :is="item.icon" /></span>
            <h2>{{ item.name }}</h2>
            <p>{{ item.desc }}</p></RouterLink
          >
        </div>
      </div>
    </section>
    <div class="home-lists">
      <div v-if="listError" class="list-error" role="status">
        暂时无法加载应用列表。<button @click="loadApps">重新加载</button>
      </div>
      <a-skeleton v-if="loading" active :paragraph="{ rows: 3 }" />
      <section v-else>
        <div class="section-heading">
          <div>
            <h2>最近创作</h2>
            <p>让每一个想法，慢慢成形。</p>
          </div>
          <RouterLink to="/my/apps">全部应用 <ArrowRightOutlined /></RouterLink>
        </div>
        <div v-if="myApps.length" class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
            @view-author="viewAuthor"
          />
        </div>
        <div v-else class="empty-projects">
          <div class="empty-icon"><LayoutOutlined /></div>
          <div>
            <h3>
              {{ store.loginUser.id ? '你的第一个应用，从这里开始' : '登录后，找回你的创作' }}
            </h3>
            <p>
              {{
                store.loginUser.id
                  ? '描述一个想法，或挑选一个模板。'
                  : '保存应用，继续对话，把想法变得更完整。'
              }}
            </p>
          </div>
          <RouterLink :to="store.loginUser.id ? '/app/new' : '/user/login'"
            >{{ store.loginUser.id ? '新建应用' : '去登录' }} <ArrowRightOutlined
          /></RouterLink>
        </div>
        <div class="section-heading featured-heading">
          <div>
            <h2>精选作品</h2>
            <p>看看大家正在创造什么。</p>
          </div>
        </div>
        <div v-if="featuredApps.length" class="app-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            featured
            @view-chat="viewChat"
            @view-work="viewWork"
            @view-author="viewAuthor"
          />
        </div>
        <p v-else class="quiet-empty">精选作品还在准备中，先从自己的想法开始吧。</p>
      </section>
    </div>
    <AuthorInfoModal v-model:open="authorVisible" :user="author" />
  </div>
</template>
<style scoped>
.home-hero {
  position: relative;
  min-height: 650px;
  overflow: hidden;
}
.home-inner {
  width: min(920px, 100%);
  padding: 88px 40px 50px;
  margin: auto;
  position: relative;
  z-index: 1;
}
.welcome-label {
  font-size: 12px;
  color: #7b8794;
  margin-bottom: 22px;
}
.home-intro {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
h1 {
  margin: 0 0 34px;
  font-size: 46px;
}
.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-top: 30px;
}
.shortcut {
  padding: 22px;
  background: rgba(255, 255, 255, 0.54);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 20px;
  box-shadow: 0 6px 15px rgba(48, 66, 92, 0.025);
  color: var(--ink);
  backdrop-filter: blur(10px);
}
.shortcut:hover {
  border-color: #bcd0ec;
  background: rgba(255, 255, 255, 0.83);
}
.shortcut-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  color: #5c82f4;
  background: linear-gradient(135deg, #f0f4ff, #dfe7fa);
  font-size: 21px;
}
.shortcut:nth-child(3) .shortcut-icon {
  color: #8880e8;
  background: #eeebfb;
}
.shortcut h2 {
  font-size: 14px;
  font-weight: 500;
  margin: 15px 0 6px;
}
.shortcut p {
  font-size: 11px;
  color: #87909c;
  margin: 0;
}
.home-lists {
  width: min(920px, 100%);
  margin: auto;
  padding: 12px 40px 60px;
}
.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.section-heading h2 {
  font-family: var(--display);
  font-size: 24px;
  font-weight: 500;
  margin: 0 0 7px;
}
.section-heading p {
  font-size: 12px;
  color: var(--muted);
  margin: 0;
}
.section-heading > a {
  font-size: 12px;
  color: #768392;
}
.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}
.empty-projects {
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px dashed #d6dee5;
  border-radius: 18px;
  padding: 26px;
  background: rgba(255, 255, 255, 0.36);
}
.empty-icon {
  font-size: 26px;
  color: #93acc9;
}
.empty-projects h3 {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 7px;
}
.empty-projects p {
  color: var(--muted);
  font-size: 12px;
  margin: 0;
}
.empty-projects > a {
  margin-left: auto;
  white-space: nowrap;
  font-size: 12px;
}
.featured-heading {
  margin-top: 48px;
}
.quiet-empty {
  color: var(--muted);
  font-size: 13px;
  padding: 20px 0;
}
.list-error {
  background: #f0f3f7;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 25px;
  font-size: 12px;
  color: #697685;
}
.list-error button {
  border: 0;
  background: none;
  color: var(--blue);
}
@media (max-width: 1050px) {
  h1 {
    font-size: 40px;
  }
  .home-inner {
    padding-top: 60px;
  }
  .shortcut {
    padding: 18px 15px;
  }
}
@media (max-width: 640px) {
  .home-intro {
    display: block;
  }
  .home-inner {
    padding: 44px 20px 30px;
  }
  h1 {
    font-size: 36px;
  }
  .home-hero {
    min-height: 0;
  }
  .shortcut-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .home-lists {
    padding: 30px 20px;
  }
  .app-grid {
    grid-template-columns: 1fr;
  }
  .empty-projects {
    flex-wrap: wrap;
    padding: 20px;
  }
  .empty-projects > a {
    margin-left: 46px;
  }
}
</style>
