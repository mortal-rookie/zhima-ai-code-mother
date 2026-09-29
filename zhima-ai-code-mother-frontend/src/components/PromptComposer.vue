<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { motionSurface as vMotionSurface } from '@/utils/motion'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { isAxiosError } from 'axios'
import {
  ArrowRightOutlined,
  LoadingOutlined,
  ThunderboltOutlined,
  AppstoreOutlined,
} from '@ant-design/icons-vue'
import { addApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { DAILY_FREE_GEN_QUOTA, resolveTodayGenUsed } from '@/config/genQuota'
const props = withDefaults(
  defineProps<{ initialPrompt?: string; placeholder?: string; showStyles?: boolean }>(),
  { initialPrompt: '', placeholder: '描述你的想法，比如：做一个课程管理系统…', showStyles: false },
)
const route = useRoute()
const draftKey = `zhima-prompt-draft:${String(route.query.template || 'free')}`
let saved: { prompt?: string; style?: string } = {}
try {
  saved = JSON.parse(sessionStorage.getItem(draftKey) || '{}')
} catch {
  /* draft storage is optional */
}
const prompt = ref(saved.prompt ?? props.initialPrompt)
const creating = ref(false)
const errorText = ref('')
const style = ref(saved.style || '现代简约')
const router = useRouter()
const store = useLoginUserStore()
const remaining = computed(() =>
  Math.max(0, DAILY_FREE_GEN_QUOTA - resolveTodayGenUsed(store.loginUser)),
)
watch(
  () => props.initialPrompt,
  (value) => {
    if (value) prompt.value = value
  },
)
const saveDraft = () => {
  try {
    sessionStorage.setItem(draftKey, JSON.stringify({ prompt: prompt.value, style: style.value }))
  } catch {
    /* keep editing without storage */
  }
}
watch([prompt, style], () => {
  saveDraft()
  errorText.value = ''
})
async function create() {
  if (creating.value) return
  if (!prompt.value.trim()) {
    errorText.value = '先描述你想创建的应用。'
    return
  }
  saveDraft()
  if (!store.loginUser.id) {
    await router.push({ path: '/user/login', query: { redirect: route.fullPath } })
    return
  }
  creating.value = true
  errorText.value = ''
  try {
    const initPrompt =
      prompt.value.trim() + (props.showStyles ? `\n视觉风格：${style.value}。` : '')
    const res = await addApp({ initPrompt })
    if (res.data.code !== 0 || !res.data.data)
      throw new Error(res.data.message || '创建失败，请重试')
    try {
      sessionStorage.removeItem(draftKey)
    } catch {
      /* optional storage */
    }
    await router.push(`/app/chat/${String(res.data.data)}`)
  } catch (error) {
    errorText.value = isAxiosError(error)
      ? error.response?.data?.message || '暂时无法连接服务，请稍后重试。'
      : error instanceof Error
        ? error.message
        : '创建失败，请重试。'
    message.error(errorText.value)
  } finally {
    creating.value = false
  }
}
</script>
<template>
  <div class="composer-area">
    <form
      v-motion-surface
      class="composer"
      :class="{ 'is-creating': creating }"
      @submit.prevent="create"
    >
      <label class="sr-only" for="application-prompt">描述你想创建的应用</label
      ><textarea
        id="application-prompt"
        v-model="prompt"
        :placeholder="placeholder"
        maxlength="1000"
        :disabled="creating"
        @keydown.ctrl.enter.prevent="create"
        @keydown.meta.enter.prevent="create"
      ></textarea>
      <div class="composer-tools">
        <span class="tool-chip selected"><ThunderboltOutlined /> 智能生成</span
        ><span class="tool-chip"><AppstoreOutlined /> 网页应用</span
        ><select
          v-if="showStyles"
          v-model="style"
          class="tool-chip style-select"
          aria-label="应用视觉风格"
          :disabled="creating"
        >
          <option>现代简约</option>
          <option>温暖自然</option>
          <option>科技深色</option>
          <option>清新明亮</option></select
        ><button
          class="send-button"
          type="submit"
          :disabled="creating"
          :aria-label="creating ? '正在创建应用' : '创建并生成应用'"
        >
          <LoadingOutlined v-if="creating" spin /><ArrowRightOutlined v-else />
        </button>
      </div>
    </form>
    <div class="composer-caption">
      <span v-if="creating">正在为你的想法创建应用…</span
      ><span v-else
        >把想法写下来，剩下的交给峙码。<span class="keyboard-tip">⌘ / Ctrl + Enter 生成</span></span
      ><span v-if="store.loginUser.id">{{
        store.loginUser.userRole === 'admin'
          ? '管理员 · 生成次数不限'
          : `今日剩余 ${remaining} 次生成`
      }}</span>
    </div>
    <Transition name="feedback"
      ><p v-if="errorText" class="composer-error" role="alert">{{ errorText }}</p></Transition
    >
  </div>
</template>
<style scoped>
.composer {
  position: relative;
  padding: 24px 24px 18px;
  border: 1px solid rgba(222, 224, 228, 0.7);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 36px rgba(43, 58, 81, 0.08);
  backdrop-filter: blur(20px);
}
.composer:focus-within {
  border-color: #b9cbed;
  box-shadow: 0 18px 40px rgba(72, 101, 152, 0.1);
}
textarea {
  display: block;
  width: 100%;
  min-height: 73px;
  padding: 0;
  color: var(--ink);
  background: transparent;
  border: 0;
  outline: 0;
  resize: vertical;
  font-size: 14px;
  line-height: 1.8;
}
textarea::placeholder {
  color: #8c959f;
}
.composer-tools {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  padding-right: 55px;
  min-height: 35px;
  flex-wrap: wrap;
}
.tool-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 1px solid rgba(33, 52, 74, 0.07);
  padding: 8px 13px;
  border-radius: 11px;
  background: #f9f8f7;
  font-size: 12px;
  color: #5c6572;
}
.tool-chip.selected {
  background: #eef3ff;
  border-color: #e3ecff;
  color: #436bea;
}
.style-select {
  max-width: 150px;
  outline-color: var(--blue);
}
.send-button {
  position: absolute;
  right: 24px;
  bottom: 20px;
  width: 43px;
  height: 43px;
  display: grid;
  place-items: center;
  border: 1px solid #e7e9ed;
  border-radius: 50%;
  background: #f7f8f9;
  color: #243950;
  font-size: 18px;
  box-shadow: 0 3px 4px rgba(50, 70, 95, 0.04);
}
.send-button:hover {
  background: #e9effb;
}
.send-button:disabled {
  opacity: 0.5;
}
.composer-caption {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  padding: 13px 4px 0;
  color: #939aa3;
  font-size: 11px;
}
.keyboard-tip {
  margin-left: 16px;
}
.composer-error {
  color: #b84d44;
  font-size: 13px;
  margin: 10px 4px;
}
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
}
@media (max-width: 640px) {
  .composer {
    padding: 20px 18px 16px;
    border-radius: 20px;
  }
  .tool-chip {
    padding: 7px 10px;
    font-size: 11px;
  }
  .send-button {
    right: 16px;
  }
  .keyboard-tip {
    display: none;
  }
}
</style>
