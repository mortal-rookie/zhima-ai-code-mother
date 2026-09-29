<script setup lang="ts">
import { ref } from 'vue'
const opened = ref<number | null>(0)
const guides = [
  {
    title: '怎样描述一个好想法？',
    text: '先说明谁会使用、解决什么问题，再列出页面和关键操作。比如：做一个课程管理网站，老师可以查看课程列表、录入学生成绩，并按课程筛选。也可以补充颜色、布局和你喜欢的风格。',
  },
  {
    title: '模板如何使用？',
    text: '模板是一段可编辑的需求描述。选择后会填入新建页，修改成自己的需求，再点击生成。模板不是现成的后端系统，数据演示与真实服务需要在需求中区分。',
  },
  {
    title: '生成次数怎么算？',
    text: '普通用户每天有 2 次免费生成，每次对话生成或继续修改都会计入。额度按服务器日期重置。当前服务在生成开始前扣次数，生成失败也可能已经扣除。管理员不受每日次数限制。',
  },
  {
    title: '为什么生成结束了，预览还没有显示？',
    text: 'Vue 应用在代码生成结束后还需要安装依赖和构建，首次构建可能较慢。稍后点击「刷新预览」。生成完成与预览就绪是两个阶段。',
  },
  {
    title: '如何修改、发布和下载？',
    text: '在自己的应用中继续描述修改需求，也可以用编辑模式选中预览元素后提出修改。完成后使用部署功能获得访问地址，或下载源码 ZIP。生成、部署与下载仅应用作者可用。',
  },
]
</script>
<template>
  <div class="page-wrap resources-page">
    <p class="page-kicker">资源库 / 使用指南</p>
    <h1 class="display-title">把想法说清楚，<br />让创作更轻松。</h1>
    <p class="page-subtitle">从第一次生成，到发布你的应用。</p>
    <div class="guide-list">
      <section v-for="(guide, index) in guides" :key="guide.title">
        <h2>
          <button
            :aria-expanded="opened === index"
            :aria-controls="`guide-${index}`"
            @click="opened = opened === index ? null : index"
          >
            {{ guide.title }}<span>{{ opened === index ? '−' : '+' }}</span>
          </button>
        </h2>
        <div
          class="guide-answer"
          :class="{ expanded: opened === index }"
          :inert="opened !== index"
          :aria-hidden="opened !== index"
          :id="`guide-${index}`"
        >
          <div>
            <p>{{ guide.text }}</p>
          </div>
        </div>
      </section>
    </div>
    <div class="resource-links">
      <RouterLink class="dark-button" to="/app/new">开始创建</RouterLink
      ><a
        href="https://github.com/mortal-rookie/zhima-ai-code-mother"
        target="_blank"
        rel="noopener noreferrer"
        >查看开源代码 ↗</a
      >
    </div>
  </div>
</template>
<style scoped>
h1 {
  font-size: 38px;
  margin: 0 0 16px;
}
.guide-list {
  margin-top: 40px;
  max-width: 780px;
}
section {
  border-bottom: 1px solid var(--line);
}
h2 {
  margin: 0;
}
h2 button {
  display: flex;
  justify-content: space-between;
  width: 100%;
  text-align: left;
  padding: 24px 0;
  background: none;
  border: 0;
  font-size: 15px;
  color: #43566b;
}
h2 span {
  color: #899aab;
}
section p {
  color: var(--muted);
  line-height: 2;
  font-size: 13px;
  padding: 0 28px 24px 0;
  margin: 0;
  max-width: 680px;
}
.resource-links {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-wrap: wrap;
  margin-top: 38px;
  font-size: 13px;
}
@media (max-width: 640px) {
  h1 {
    font-size: 30px;
  }
}
</style>
