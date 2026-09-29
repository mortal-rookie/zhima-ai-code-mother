<script setup lang="ts">
import BrandLogo from '@/components/BrandLogo.vue'
import { motionSurface as vMotionSurface } from '@/utils/motion'
import AtmosphereArt from '@/components/AtmosphereArt.vue'
import { ArrowRightOutlined } from '@ant-design/icons-vue'
</script>
<template>
  <div class="welcome-page">
    <header>
      <RouterLink to="/welcome" aria-label="峙码首页"><BrandLogo /></RouterLink>
      <nav aria-label="产品导航">
        <RouterLink to="/">产品</RouterLink><RouterLink to="/templates">模板</RouterLink
        ><RouterLink to="/resources">使用指南</RouterLink
        ><RouterLink class="dark-button" to="/app/new">开始创作</RouterLink>
      </nav>
    </header>
    <section v-motion-surface class="welcome-hero">
      <AtmosphereArt variant="panels" />
      <div class="welcome-copy">
        <h1 class="display-title">用自然语言，<br />创造属于你的应用</h1>
        <h2>峙码 · 零代码应用生成平台</h2>
        <p>把想法变成真实可用的应用，从未如此简单。</p>
        <RouterLink class="dark-button" to="/app/new">开始创作 <ArrowRightOutlined /></RouterLink>
      </div>
      <div class="hero-note">从一个想法，到一个可以打开的网站。</div>
    </section>
    <section class="welcome-bottom">
      <p>你负责想象，峙码负责实现。</p>
      <RouterLink to="/templates">从模板寻找灵感 <ArrowRightOutlined /></RouterLink>
    </section>
  </div>
</template>
<style scoped>
.welcome-page {
  background: radial-gradient(ellipse at 75% 45%, #f8e3cd, transparent 45%), #f8f7f5;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1440px;
  padding: 30px 64px;
  margin: auto;
}
nav {
  display: flex;
  align-items: center;
  gap: 35px;
}
nav > a {
  color: var(--ink);
  font-size: 13px;
}
nav > .dark-button {
  color: white;
  padding: 11px 26px;
}
.welcome-hero {
  position: relative;
  min-height: 660px;
  max-width: 1440px;
  margin: auto;
  overflow: hidden;
}
.welcome-copy {
  position: relative;
  padding: 98px 64px 120px;
  z-index: 1;
}
h1 {
  font-size: clamp(38px, 4.2vw, 64px);
  margin: 0 0 26px;
}
h2 {
  font-family: var(--display);
  font-weight: 400;
  font-size: 23px;
  margin: 0 0 10px;
}
.welcome-copy p {
  color: var(--muted);
  font-size: 15px;
  margin-bottom: 42px;
}
.hero-note {
  position: absolute;
  bottom: 28px;
  left: 64px;
  color: #5c778c;
  font-size: 12px;
}
.welcome-bottom {
  padding: 30px 64px;
  display: flex;
  justify-content: space-between;
  max-width: 1440px;
  margin: auto;
  font-size: 13px;
  color: var(--muted);
}
.welcome-bottom p {
  margin: 0;
}
@media (max-width: 900px) {
  header {
    padding: 24px;
  }
  nav {
    gap: 20px;
  }
  .welcome-copy {
    padding: 95px 32px 120px;
  }
  .welcome-hero {
    min-height: 630px;
  }
  h2 {
    font-size: 19px;
  }
  .hero-note {
    left: 32px;
  }
}
@media (max-width: 640px) {
  nav > a:not(.dark-button) {
    display: none;
  }
  .welcome-copy {
    padding: 70px 24px 200px;
  }
  h1 {
    font-size: 36px;
  }
  .welcome-hero {
    min-height: 620px;
  }
  .welcome-bottom {
    padding: 24px;
    gap: 20px;
    flex-direction: column;
  }
  .hero-note {
    left: 24px;
  }
}
</style>
