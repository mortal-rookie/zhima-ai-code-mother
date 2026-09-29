<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { CloseOutlined, HeartOutlined } from '@ant-design/icons-vue'
import spriteSheet from '@/assets/klee/klee-sprites.png'

const root = ref<HTMLElement>()
const hidden = ref(false)
const greeting = ref(false)
const celebrating = ref(false)
const paused = ref(false)
const speech = ref('')
const bubbleText = computed(() => speech.value || (greeting.value ? '嗨，一起创造点什么吧！' : ''))
const messages = ['一起把灵感变成小作品吧！', '嘿嘿，新的冒险开始啦！', '今天也要开心创作哦！']
let messageIndex = 0
let celebrationTimer: ReturnType<typeof setTimeout> | undefined
let speechTimer: ReturnType<typeof setTimeout> | undefined
let observer: IntersectionObserver | undefined
let visible = true
try {
  hidden.value = localStorage.getItem('zhima-klee-hidden') === 'true'
} catch {
  /* Optional preference. */
}

function setHidden(value: boolean) {
  hidden.value = value
  greeting.value = celebrating.value = false
  speech.value = ''
  clearTimeout(celebrationTimer)
  clearTimeout(speechTimer)
  try {
    localStorage.setItem('zhima-klee-hidden', String(value))
  } catch {
    /* Keep the session preference. */
  }
}
function celebrate() {
  if (celebrating.value) return
  celebrating.value = true
  speech.value = messages[messageIndex++ % messages.length]!
  clearTimeout(speechTimer)
  celebrationTimer = setTimeout(() => {
    celebrating.value = false
  }, 900)
  speechTimer = setTimeout(() => {
    speech.value = ''
  }, 3200)
}
const updatePause = () => {
  paused.value = !visible || document.hidden
}
onMounted(() => {
  observer = new IntersectionObserver(([entry]) => {
    visible = !!entry?.isIntersecting
    updatePause()
  })
  if (root.value) observer.observe(root.value)
  document.addEventListener('visibilitychange', updatePause)
  updatePause()
})
onBeforeUnmount(() => {
  clearTimeout(celebrationTimer)
  clearTimeout(speechTimer)
  observer?.disconnect()
  document.removeEventListener('visibilitychange', updatePause)
})
</script>

<template>
  <div
    ref="root"
    class="klee-friend"
    :class="{ hidden, greeting, celebrating, 'is-paused': paused }"
  >
    <template v-if="!hidden">
      <Transition name="klee-bubble">
        <div v-if="bubbleText" class="klee-bubble" role="status" aria-live="polite">
          {{ bubbleText }}
        </div>
      </Transition>
      <button
        class="klee-dismiss"
        type="button"
        aria-label="暂时隐藏可莉"
        title="暂时隐藏可莉"
        @click="setHidden(true)"
      >
        <CloseOutlined />
      </button>
      <button
        class="klee-character"
        type="button"
        aria-label="和可莉打招呼"
        @pointerenter="greeting = true"
        @pointerleave="greeting = false"
        @focus="greeting = true"
        @blur="greeting = false"
        @click="celebrate"
      >
        <span class="klee-shadow" aria-hidden="true"></span>
        <span class="klee-art" aria-hidden="true"
          ><span class="klee-sprite" :style="{ backgroundImage: `url(${spriteSheet})` }"></span
        ></span>
      </button>
      <div v-if="celebrating" class="klee-sparkles" aria-hidden="true">
        <i v-for="star in 5" :key="star">✦</i>
      </div>
    </template>
    <button
      v-else
      class="klee-summon"
      type="button"
      aria-label="召唤可莉"
      @click="setHidden(false)"
    >
      <HeartOutlined /> 召唤可莉
    </button>
  </div>
</template>

<style scoped>
.klee-friend {
  position: relative;
  width: 164px;
  height: 184px;
  flex-shrink: 0;
  align-self: center;
  margin-top: -8px;
}
.klee-character {
  display: block;
  position: relative;
  width: 164px;
  height: 164px;
  padding: 0;
  border: 0;
  border-radius: 45%;
  background: transparent;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}
.klee-art {
  display: block;
  width: 100%;
  height: 100%;
  transform-origin: 50% 90%;
  animation: klee-breathe 3.6s ease-in-out infinite;
  filter: drop-shadow(0 5px 5px rgba(153, 111, 91, 0.08));
}
.klee-sprite {
  display: block;
  width: 100%;
  height: 100%;
  background-size: 200% 200%;
  background-position: 0 0;
  background-repeat: no-repeat;
  animation: klee-blink 6.8s step-end infinite;
}
.klee-shadow {
  position: absolute;
  left: 29%;
  bottom: 4px;
  width: 42%;
  height: 10px;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(148, 126, 109, 0.19), transparent 70%);
  animation: klee-shadow 3.6s ease-in-out infinite;
}
.greeting .klee-sprite {
  animation: none;
  background-position: 0 100%;
}
.greeting .klee-art {
  animation: klee-wave 1.1s ease-in-out infinite;
}
.celebrating .klee-sprite {
  animation: none;
  background-position: 100% 100%;
}
.celebrating .klee-art {
  animation: klee-hop 900ms cubic-bezier(0.22, 1, 0.36, 1) both;
}
.celebrating .klee-shadow {
  animation: klee-hop-shadow 900ms ease both;
}
.is-paused .klee-art,
.is-paused .klee-sprite,
.is-paused .klee-shadow {
  animation-play-state: paused;
}
.klee-dismiss {
  position: absolute;
  top: 0;
  right: 3px;
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(178, 155, 138, 0.16);
  border-radius: 50%;
  color: #aa9284;
  background: rgba(255, 255, 255, 0.65);
  z-index: 2;
  font-size: 10px;
  opacity: 0.5;
  transition:
    opacity 200ms ease,
    transform 300ms ease;
}
.klee-friend:hover .klee-dismiss,
.klee-friend:focus-within .klee-dismiss {
  opacity: 1;
}
.klee-dismiss:hover {
  transform: rotate(90deg);
}
.klee-bubble {
  position: absolute;
  bottom: calc(100% - 14px);
  right: 9px;
  width: max-content;
  max-width: 210px;
  padding: 11px 15px;
  border: 1px solid #f0ddd1;
  border-radius: 15px 15px 4px 15px;
  color: #936653;
  background: rgba(255, 250, 246, 0.97);
  box-shadow: 0 8px 20px rgba(163, 123, 104, 0.08);
  font-size: 12px;
  line-height: 1.7;
  pointer-events: none;
  z-index: 4;
}
.klee-bubble-enter-active,
.klee-bubble-leave-active {
  transition:
    opacity 200ms ease,
    transform 300ms var(--ease-spring);
}
.klee-bubble-enter-from,
.klee-bubble-leave-to {
  opacity: 0;
  transform: translateY(6px) scale(0.94);
}
.klee-sparkles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.klee-sparkles i {
  position: absolute;
  left: 50%;
  top: 35%;
  color: #efbd6f;
  font-size: 17px;
  font-style: normal;
  animation: klee-spark 900ms var(--ease-soft) both;
  --star-x: -70px;
  --star-y: -40px;
}
.klee-sparkles i:nth-child(2) {
  --star-x: 65px;
  --star-y: -45px;
  animation-delay: 40ms;
  color: #df8d81;
}
.klee-sparkles i:nth-child(3) {
  --star-x: -50px;
  --star-y: 30px;
  animation-delay: 70ms;
}
.klee-sparkles i:nth-child(4) {
  --star-x: 65px;
  --star-y: 35px;
  animation-delay: 90ms;
  color: #e2a091;
}
.klee-sparkles i:nth-child(5) {
  --star-x: 8px;
  --star-y: -70px;
  animation-delay: 30ms;
}
.klee-summon {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 11px;
  border: 1px solid #eedfd6;
  border-radius: 999px;
  color: #a47460;
  background: rgba(255, 249, 244, 0.75);
  font-size: 11px;
  white-space: nowrap;
  transition:
    background 200ms ease,
    transform 300ms var(--ease-spring);
}
.klee-summon:hover {
  background: #fff6ee;
  transform: translateY(-2px);
}
.hidden {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
@keyframes klee-blink {
  0%,
  94%,
  98%,
  100% {
    background-position: 0 0;
  }
  95%,
  97% {
    background-position: 100% 0;
  }
}
@keyframes klee-breathe {
  50% {
    transform: translateY(-4px) rotate(1.5deg);
  }
}
@keyframes klee-shadow {
  50% {
    transform: scale(0.9);
    opacity: 0.7;
  }
}
@keyframes klee-wave {
  0%,
  100% {
    transform: rotate(-3deg) translateY(-2px);
  }
  50% {
    transform: rotate(3deg) translateY(-5px);
  }
}
@keyframes klee-hop {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }
  15% {
    transform: translateY(3px) scale(1.05, 0.94);
  }
  40% {
    transform: translateY(-22px) rotate(-5deg) scale(0.98, 1.03);
  }
  65% {
    transform: translateY(1px) rotate(3deg) scale(1.04, 0.97);
  }
  80% {
    transform: translateY(-4px);
  }
}
@keyframes klee-hop-shadow {
  40% {
    transform: scale(0.65);
    opacity: 0.4;
  }
}
@keyframes klee-spark {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.2);
  }
  25% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate(var(--star-x), var(--star-y)) scale(0.7) rotate(60deg);
  }
}
@media (min-width: 641px) and (max-width: 1050px) {
  .klee-friend {
    width: 120px;
    height: 144px;
  }
  .klee-character {
    width: 120px;
    height: 120px;
  }
}
@media (max-width: 640px) {
  .klee-friend {
    position: absolute;
    right: -4px;
    top: -16px;
    width: 96px;
    height: 96px;
    margin-top: 0;
  }
  .klee-character {
    width: 96px;
    height: 96px;
  }
  .klee-dismiss {
    top: 1px;
    right: -3px;
    width: 20px;
    height: 20px;
    font-size: 8px;
  }
  .klee-bubble {
    right: 5px;
    bottom: calc(100% - 14px);
    padding: 8px 11px;
    max-width: 200px;
    font-size: 11px;
  }
  .klee-summon {
    padding: 7px 9px;
    font-size: 10px;
  }
  .hidden {
    top: 0;
    height: 26px;
  }
  .klee-sparkles i {
    font-size: 13px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .klee-art {
    transform: none !important;
  }
  .klee-sparkles {
    display: none;
  }
}
</style>
