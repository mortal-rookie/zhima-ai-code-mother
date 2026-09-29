<script setup lang="ts">
import { computed, ref } from 'vue'
import { motionSurface as vMotionSurface } from '@/utils/motion'
import { ArrowRightOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { promptTemplates } from '@/data/templates'
import TemplateArtwork from '@/components/TemplateArtwork.vue'
const search = ref('')
const category = ref('全部')
const categories = ['全部', '办公', '学习', '数据', 'AI', '生活']
function freezeLeavingCard(element: Element) {
  const card = element as HTMLElement
  const width = card.offsetWidth
  const height = card.offsetHeight
  const left = card.offsetLeft
  const top = card.offsetTop
  Object.assign(card.style, {
    width: `${width}px`,
    height: `${height}px`,
    left: `${left}px`,
    top: `${top}px`,
  })
  card.inert = true
}
function restoreCard(element: Element) {
  const card = element as HTMLElement
  for (const property of ['width', 'height', 'left', 'top']) card.style.removeProperty(property)
  card.inert = false
}
const filtered = computed(() =>
  promptTemplates.filter(
    (t) =>
      (category.value === '全部' || category.value === t.category) &&
      `${t.name}${t.description}${t.prompt}`.includes(search.value.trim()),
  ),
)
</script>
<template>
  <div class="page-wrap templates-page">
    <p class="page-kicker">模板</p>
    <div class="templates-heading">
      <div>
        <h1 class="display-title">从灵感出发，<br />快速创建你的应用</h1>
        <p class="page-subtitle">
          精选需求模板，覆盖学习、办公、数据等场景。<br />选择一个起点，再把它变成你的想法。
        </p>
      </div>
      <div class="template-search">
        <SearchOutlined /><input
          v-model="search"
          placeholder="搜索模板…"
          aria-label="搜索模板"
        /><button v-if="search" @click="search = ''" aria-label="清除搜索">×</button>
      </div>
    </div>
    <div class="category-tabs" aria-label="模板分类">
      <button
        v-for="item in categories"
        :key="item"
        :class="{ selected: category === item }"
        :aria-pressed="category === item"
        @click="category = item"
      >
        {{ item }}
      </button>
    </div>
    <TransitionGroup
      name="template"
      tag="div"
      class="template-grid"
      @before-leave="freezeLeavingCard"
      @before-enter="restoreCard"
      @leave-cancelled="restoreCard"
    >
      <RouterLink
        v-for="item in filtered"
        :key="item.id"
        :to="{ path: '/app/new', query: { template: item.id } }"
        class="template-card"
        v-motion-surface
        ><TemplateArtwork :tone="item.art" />
        <div class="template-info">
          <h2>{{ item.name }}</h2>
          <p>{{ item.description }} <ArrowRightOutlined /></p></div
      ></RouterLink>
    </TransitionGroup>
    <div v-if="!filtered.length" class="template-empty">
      <h2>没有找到相关模板</h2>
      <p>试试其他关键词，或者从自己的想法开始。</p>
      <RouterLink class="dark-button" to="/app/new">自由创建</RouterLink>
    </div>
    <p class="template-note">模板提供可编辑的需求描述，应用由 AI 生成。</p>
  </div>
</template>
<style scoped>
.templates-heading {
  display: flex;
  justify-content: space-between;
  gap: 28px;
  align-items: flex-start;
}
h1 {
  font-size: 38px;
  margin: 0 0 15px;
}
.template-search {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 230px;
  margin-top: 4px;
  padding: 12px 16px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.65);
  box-shadow: 0 4px 9px rgba(50, 70, 90, 0.025);
  color: #84909e;
}
input {
  width: 100%;
  min-width: 0;
  outline: 0;
  border: 0;
  background: transparent;
  font-size: 12px;
  color: var(--ink);
}
input::placeholder {
  color: #919ca6;
}
.template-search:focus-within {
  border-color: #a2bbe6;
}
.template-search button {
  border: 0;
  background: none;
  font-size: 18px;
  color: #75808d;
}
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 30px 0 26px;
}
.category-tabs button {
  padding: 8px 17px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: #7d8792;
  font-size: 12px;
}
.category-tabs .selected {
  background: #eaf0f9;
  color: #45658f;
  border-color: #e0e8f3;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}
.template-card {
  padding: 4px;
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 21px;
  background: rgba(255, 255, 255, 0.55);
  box-shadow: 0 8px 18px rgba(50, 70, 90, 0.035);
  color: var(--ink);
}
.template-card:hover {
  border-color: #b5c9e4;
}
.template-info {
  padding: 17px 15px 20px;
}
h2 {
  font-weight: 500;
  font-size: 15px;
  margin: 0 0 8px;
}
.template-info p {
  display: flex;
  justify-content: space-between;
  gap: 5px;
  color: #89929c;
  font-size: 11px;
  margin: 0;
}
.template-info .anticon {
  color: #6c7b8b;
}
.template-note {
  font-size: 11px;
  color: #9199a2;
  margin: 25px 0 0;
}
.template-empty {
  padding: 70px 0;
  text-align: center;
  color: var(--muted);
}
@media (max-width: 1150px) {
  .template-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 640px) {
  .templates-heading {
    flex-direction: column;
  }
  h1 {
    font-size: 31px;
  }
  .template-search {
    width: 100%;
  }
  .template-grid {
    gap: 12px;
  }
  .template-info {
    padding: 15px 9px;
  }
}
</style>
