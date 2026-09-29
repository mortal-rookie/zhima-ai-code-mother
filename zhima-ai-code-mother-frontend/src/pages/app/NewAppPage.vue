<script setup lang="ts">
import { computed } from 'vue'
import { motionSurface as vMotionSurface } from '@/utils/motion'
import { useRoute } from 'vue-router'
import AtmosphereArt from '@/components/AtmosphereArt.vue'
import PromptComposer from '@/components/PromptComposer.vue'
import { promptTemplates } from '@/data/templates'
const route = useRoute()
const template = computed(() => promptTemplates.find((t) => t.id === route.query.template))
</script>
<template>
  <div v-motion-surface class="new-page">
    <AtmosphereArt variant="dashboard" />
    <div class="page-wrap new-inner">
      <p class="page-kicker">
        <RouterLink to="/app/new">新建应用</RouterLink><span> / </span>应用生成
      </p>
      <div class="new-heading">
        <h1 class="display-title">用自然语言，<br />生成你的应用</h1>
        <p class="page-subtitle">
          描述需求，选择风格，峙码将为你生成应用。<br />你可以继续通过对话进行修改。
        </p>
      </div>
      <div v-if="template" class="template-selected">
        从「{{ template.name }}」开始 <RouterLink to="/templates">更换模板</RouterLink>
      </div>
      <PromptComposer
        :key="String(route.query.template || 'free')"
        :initial-prompt="template?.prompt"
        show-styles
        placeholder="帮我做一个课程管理系统，包含课程列表、学生管理和成绩统计…"
      />
      <div class="new-help">
        <span>描述得越具体，结果越贴近你的想法。</span
        ><RouterLink to="/resources">怎样写好需求？</RouterLink>
      </div>
      <div class="creation-steps">
        <div>
          <span>描述想法</span>
          <p>说明用途、页面与功能</p>
        </div>
        <i></i>
        <div>
          <span>对话完善</span>
          <p>预览结果，继续调整</p>
        </div>
        <i></i>
        <div>
          <span>发布分享</span>
          <p>部署网站或下载源码</p>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.new-page {
  position: relative;
  min-height: calc(100dvh - 70px);
  overflow: hidden;
}
.new-inner {
  position: relative;
  z-index: 1;
  padding-top: 55px;
  max-width: 1000px;
}
.page-kicker {
  margin-bottom: 62px;
}
.page-kicker a {
  color: #7e8996;
}
.page-kicker span {
  padding: 0 8px;
  color: #c1c7ce;
}
.new-heading {
  margin-bottom: 50px;
}
h1 {
  font-size: 43px;
  margin: 0 0 20px;
}
.new-inner :deep(.composer-area) {
  max-width: 760px;
}
.new-help {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: space-between;
  max-width: 760px;
  margin: 28px 0 0;
  font-size: 12px;
  color: #87929f;
}
.new-help a {
  color: #6b84a6;
}
.template-selected {
  font-size: 12px;
  color: #647a96;
  margin-bottom: 14px;
}
.template-selected a {
  margin-left: 15px;
}
.creation-steps {
  max-width: 760px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 85px;
  gap: 20px;
}
.creation-steps span {
  font-size: 13px;
  color: #627487;
}
.creation-steps p {
  font-size: 11px;
  color: #949ea9;
  margin: 8px 0 0;
}
.creation-steps i {
  width: 45px;
  height: 1px;
  background: #d6dfe7;
}
@media (max-width: 640px) {
  .page-kicker {
    margin-bottom: 35px;
  }
  .new-heading {
    margin-bottom: 36px;
  }
  h1 {
    font-size: 35px;
  }
  .creation-steps {
    margin-top: 45px;
    gap: 12px;
    flex-wrap: wrap;
  }
  .creation-steps i {
    display: none;
  }
}
</style>
