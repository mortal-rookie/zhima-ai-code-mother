<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
const scene = ref<HTMLElement>()
let observer: IntersectionObserver | undefined
let visible = true
const pause = () => scene.value?.classList.toggle('scene-paused', !visible || document.hidden)
onMounted(() => {
  observer = new IntersectionObserver(([entry]) => {
    visible = !!entry?.isIntersecting
    pause()
  })
  if (scene.value) observer.observe(scene.value)
  document.addEventListener('visibilitychange', pause)
  pause()
})
onBeforeUnmount(() => {
  observer?.disconnect()
  document.removeEventListener('visibilitychange', pause)
})
withDefaults(defineProps<{ variant?: 'waves' | 'panels' | 'dashboard' }>(), { variant: 'waves' })
</script>
<template>
  <div ref="scene" class="atmosphere" :class="variant" aria-hidden="true">
    <div class="ambient-glow"></div>
    <div class="sun"></div>
    <svg class="waves-art" viewBox="0 0 1100 650" preserveAspectRatio="none">
      <defs>
        <linearGradient id="mist-wave" x1="0" y1="0" x2="1" y2="1">
          <stop stop-color="#eef3f6" stop-opacity=".45" />
          <stop offset=".55" stop-color="#a8cadd" stop-opacity=".48" />
          <stop offset="1" stop-color="#d8e8f3" stop-opacity=".6" />
        </linearGradient>
        <linearGradient id="warm-wave">
          <stop stop-color="#cfdfeb" />
          <stop offset=".65" stop-color="#b4cfdf" />
          <stop offset="1" stop-color="#f7e2ca" />
        </linearGradient>
      </defs>
      <path
        d="M0 430C220 400 285 475 490 310S725 215 835 250 980 120 1100 100V650H0Z"
        fill="url(#mist-wave)"
        stroke="white"
        stroke-opacity=".9"
        stroke-width="1.5"
      />
      <path
        d="M0 520C225 380 400 460 620 320S870 330 1100 225V650H0Z"
        fill="url(#mist-wave)"
        stroke="white"
        stroke-opacity=".85"
        stroke-width="1.5"
      />
      <path
        d="M0 565C240 520 260 400 525 455S830 465 1100 370V650H0Z"
        fill="url(#warm-wave)"
        opacity=".55"
        stroke="white"
        stroke-width="1.5"
      />
    </svg>
    <div v-if="variant === 'panels'" class="glass-panels">
      <div class="glass-panel panel-back"></div>
      <div class="glass-panel panel-front"></div>
    </div>
    <div v-if="variant === 'dashboard'" class="glass-dashboard">
      <div class="mock-sidebar"><i></i><i></i><i></i><i></i><i></i></div>
      <div class="mock-content">
        <div class="mock-heading"></div>
        <div class="mock-chart">
          <span></span><span></span><span></span><span></span><span></span>
        </div>
        <div class="mock-row">
          <div></div>
          <div></div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.atmosphere {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.sun {
  width: 240px;
  height: 240px;
  position: absolute;
  top: 25px;
  right: 65px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  background: radial-gradient(circle at 30% 25%, #eaf0f3, #ccdde7 45%, #f1e8e0 75%);
  box-shadow: inset 0 0 35px rgba(255, 255, 255, 0.7);
  opacity: 0.7;
}
.waves-art {
  width: 112%;
  height: 112%;
  position: absolute;
  left: -6%;
  top: -6%;
  translate: calc(var(--motion-x, 0px) * -0.6) calc(var(--motion-y, 0px) * -0.6);
}
.waves-art > path {
  transform-origin: center bottom;
  animation: wave-drift 18s ease-in-out infinite alternate;
}
.waves-art > path:nth-of-type(2) {
  animation-duration: 23s;
  animation-delay: -8s;
  animation-direction: alternate-reverse;
}
.waves-art > path:nth-of-type(3) {
  animation-duration: 21s;
  animation-delay: -12s;
}
.sun {
  translate: calc(var(--motion-x, 0px) * 1.3) calc(var(--motion-y, 0px) * 1.3);
  animation: sphere-float 16s ease-in-out infinite;
}
.glass-panels {
  translate: calc(var(--motion-x, 0px) * 1.8) calc(var(--motion-y, 0px) * 1.4);
}
.glass-panel {
  animation: panel-float 13s ease-in-out infinite;
}
.panel-front {
  animation-duration: 17s;
  animation-delay: -6s;
}
.glass-dashboard {
  translate: calc(var(--motion-x, 0px) * 1.8) calc(var(--motion-y, 0px) * 1.4);
  animation: dashboard-float 14s ease-in-out infinite;
}
.ambient-glow {
  position: absolute;
  width: 370px;
  height: 370px;
  right: 10%;
  top: 25%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 221, 184, 0.42), transparent 70%);
  animation: light-drift 24s ease-in-out infinite alternate;
}
.scene-paused *,
.scene-paused .waves-art > path {
  animation-play-state: paused;
}
@keyframes wave-drift {
  from {
    transform: translate3d(-12px, 8px, 0) scaleY(0.98);
  }
  to {
    transform: translate3d(18px, -18px, 0) scaleY(1.04);
  }
}
@keyframes sphere-float {
  50% {
    transform: translate3d(-12px, 14px, 0) scale(1.025);
  }
}
@keyframes panel-float {
  50% {
    transform: translate3d(0, -16px, 0) skewY(-21deg) rotate(1deg);
  }
}
@keyframes dashboard-float {
  50% {
    transform: perspective(900px) translate3d(0, -13px, 0) rotateY(-16deg) rotateZ(-3deg);
  }
}
@keyframes light-drift {
  to {
    transform: translate3d(-120px, -50px, 0) scale(1.25);
    opacity: 0.6;
  }
}
.panels .sun {
  display: none;
}
.panels .waves-art {
  top: 12%;
  height: 100%;
}
.glass-panels {
  position: absolute;
  inset: 0;
}
.glass-panel {
  position: absolute;
  border: 1.5px solid rgba(255, 255, 255, 0.9);
  border-right-color: #b6c4ce;
  border-radius: 26px;
  background: linear-gradient(
    145deg,
    rgba(198, 219, 235, 0.65),
    rgba(255, 255, 255, 0.3) 62%,
    rgba(255, 220, 186, 0.4)
  );
  box-shadow:
    18px 20px 55px rgba(125, 157, 177, 0.1),
    inset 0 0 18px rgba(255, 255, 255, 0.4);
  transform: skewY(-21deg);
  backdrop-filter: blur(5px);
}
.panel-back {
  right: 12%;
  top: 22%;
  width: 210px;
  height: 355px;
}
.panel-front {
  right: 27%;
  top: 44%;
  width: 185px;
  height: 260px;
}
.glass-dashboard {
  display: flex;
  gap: 15px;
  position: absolute;
  right: -30px;
  top: 155px;
  width: 430px;
  height: 300px;
  transform: perspective(900px) rotateY(-20deg) rotateZ(-5deg);
  padding: 22px;
  background: rgba(232, 242, 249, 0.4);
  border: 1px solid white;
  border-radius: 22px;
  box-shadow: 0 30px 70px rgba(78, 120, 154, 0.12);
  backdrop-filter: blur(10px);
}
.mock-sidebar {
  width: 55px;
  border-right: 1px solid #d4e2ee;
  padding: 20px 10px;
}
.mock-sidebar i {
  display: block;
  height: 7px;
  margin-bottom: 22px;
  border-radius: 5px;
  background: #b6cce0;
}
.mock-content {
  flex: 1;
}
.mock-heading {
  width: 95px;
  height: 9px;
  border-radius: 8px;
  background: #9fb7cc;
  margin: 10px 0 25px;
}
.mock-chart {
  height: 105px;
  display: flex;
  align-items: end;
  gap: 15px;
  padding: 12px 20px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16px;
}
.mock-chart span {
  flex: 1;
  border-radius: 8px 8px 3px 3px;
  background: linear-gradient(#a9c9eb, #d8e5f2);
  height: 42%;
}
.mock-chart span:nth-child(2) {
  height: 65%;
}
.mock-chart span:nth-child(3) {
  height: 48%;
}
.mock-chart span:nth-child(4) {
  height: 75%;
}
.mock-chart span:nth-child(5) {
  height: 95%;
}
.mock-row {
  display: flex;
  gap: 14px;
  margin-top: 16px;
}
.mock-row div {
  width: 50%;
  height: 65px;
  border: 1px solid white;
  border-radius: 12px;
  background: repeating-linear-gradient(0deg, transparent 0 15px, #d3e0ed 16px 20px);
  opacity: 0.6;
}
@media (max-width: 640px) {
  .sun {
    right: -50px;
    width: 200px;
    height: 200px;
  }
  .glass-dashboard {
    opacity: 0.35;
    width: 340px;
    top: 80px;
  }
  .glass-panel {
    opacity: 0.5;
  }
  .panel-back {
    right: -30px;
    top: 15%;
  }
  .panel-front {
    right: 70px;
    top: 40%;
  }
}
</style>
