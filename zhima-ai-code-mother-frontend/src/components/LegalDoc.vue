<template>
  <div class="legal-doc">
    <h1 class="doc-title">{{ title }}</h1>
    <p class="doc-meta">生效日期：{{ effectiveDate }}　|　最近更新：{{ updatedDate }}</p>
    <div class="doc-body">
      <slot />
    </div>
    <div class="doc-actions">
      <RouterLink to="/">返回主页</RouterLink>
      <template v-if="showOtherLink">
        <span class="sep">|</span>
        <RouterLink :to="otherLink">{{ otherText }}</RouterLink>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/**
 * 协议类页面的统一外壳：标题 + 生效日期 + 正文排版 + 底部返回。
 * 正文用插槽传入（用户协议 / 隐私协议各写各的），排版样式在这里统一维护。
 */
const props = withDefaults(
  defineProps<{
    title: string
    effectiveDate: string
    updatedDate: string
    otherText?: string
    otherLink?: string
  }>(),
  {
    otherText: '',
    otherLink: '',
  },
)

// 两个协议互跳：没给 otherLink 就不渲染
const showOtherLink = computed(() => !!props.otherLink)
</script>

<style scoped>
.legal-doc {
  max-width: 900px;
  margin: 32px auto 56px;
  padding: 40px 40px 32px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid var(--line);
  border-radius: 18px;
  backdrop-filter: blur(18px);
  box-shadow: 0 24px 60px rgba(48, 66, 92, 0.07);
}

.doc-title {
  margin: 0 0 12px;
  color: var(--ink);
  font-size: 26px;
  font-weight: 700;
  text-align: center;
}

.doc-meta {
  margin: 0 0 28px;
  text-align: center;
  color: var(--muted);
  font-size: 13px;
}

.doc-body {
  color: #5d6b7b;
  font-size: 14px;
  line-height: 1.9;
}

.doc-body :deep(h3) {
  position: relative;
  margin: 26px 0 10px;
  padding-left: 12px;
  color: var(--ink);
  font-size: 16px;
  font-weight: 700;
}

.doc-body :deep(h3)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 3px;
  height: 15px;
  border-radius: 2px;
  background: linear-gradient(180deg, #426be8 0%, #8da9db 100%);
}

.doc-body :deep(p) {
  margin: 0 0 10px;
}

.doc-body :deep(ul) {
  margin: 0 0 10px;
  padding-left: 22px;
}

.doc-body :deep(li) {
  margin-bottom: 6px;
}

.doc-body :deep(strong) {
  color: #405b7c;
  font-weight: 600;
}

.doc-body :deep(a) {
  color: #426be8;
}

.doc-actions {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
  text-align: center;
  font-size: 14px;
}

.doc-actions a {
  color: #426be8;
}

.doc-actions .sep {
  margin: 0 12px;
  color: #9aa5b0;
}

@media (max-width: 768px) {
  .legal-doc {
    margin: 16px 12px 32px;
    padding: 24px 18px 20px;
  }
}
</style>
