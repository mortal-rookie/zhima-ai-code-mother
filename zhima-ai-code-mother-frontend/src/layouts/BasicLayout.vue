<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏（整条黑色区域点空白也可返回主页，导航卡片/Logo/头像下拉照旧走自己的逻辑） -->
    <GlobalHeader @click="onBarClick" />
    <!-- 主要内容区域 -->
    <!-- 点空白处返回主页：
         内容区必须用 .self —— 只有点击目标就是这块内容区本身（也就是卡片/表格之外的空白）才触发，
         点在卡片、表格、按钮这些子元素上不会误触。
         为什么不写在各个页面根节点上：那些页面根节点带 max-width + margin:auto，
         两侧和下方的空白压根不属于它们，写在那边是点不中的。 -->
    <a-layout-content class="main-content" @click.self="onBlankClick">
      <router-view />
    </a-layout-content>
    <!-- 底部版权信息（整条黑色区域点击返回主页） -->
    <GlobalFooter @click="onBarClick" />
  </a-layout>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const router = useRouter()
const route = useRoute()

/**
 * 这些页面不参与「点空白回主页」。
 * 对话页正在跑 SSE 流式生成，点一下空白就被跳走会把流断掉、这一轮生成白费，
 * 所以在生成/编辑页保持原样。
 */
const NO_BLANK_NAV_PREFIXES = ['/app/chat', '/app/edit']

// 回到主页（已是主页、或处于生成/编辑页时不跳）
const goHome = () => {
  const path = route.path
  if (path === '/') {
    return
  }
  if (NO_BLANK_NAV_PREFIXES.some((prefix) => path.startsWith(prefix))) {
    return
  }
  router.push('/')
}

// 点击内容区空白处返回主页
const onBlankClick = () => {
  goHome()
}

/**
 * 顶栏 / 底栏那两条黑条上，点空白也要能返回主页。
 * 这两处不能用 .self：顶栏里 logo、导航卡片、头像下拉把整行铺满了，
 * 点在黑条上时事件目标其实是内层的 col / 菜单容器，.self 永远打不中。
 * 所以改成从点击目标往上溯源，只要路径上遇到可交互元素就交还给元素自己处理，
 * 剩下真正的空白才回主页。底部那条只有一个版权文本，溯源后必定落到空白分支。
 */
const INTERACTIVE_TAGS = ['a', 'button', 'input', 'textarea', 'select', 'svg']
const INTERACTIVE_CLASSES = [
  'ant-menu-item',
  'ant-menu-submenu',
  'ant-dropdown',
  'ant-dropdown-trigger',
  'ant-btn',
  'ant-avatar',
  'ant-space',
  'ant-space-item',
]

const isInteractive = (target: EventTarget | null): boolean => {
  let el = target as HTMLElement | null
  while (el && el !== document.body) {
    const tag = el.tagName?.toLowerCase()
    if (tag && INTERACTIVE_TAGS.includes(tag)) {
      return true
    }
    if (el.classList?.contains && INTERACTIVE_CLASSES.some((cls) => el!.classList.contains(cls))) {
      return true
    }
    el = el.parentElement
  }
  return false
}

// 点击顶栏 / 底栏的黑色空白处返回主页
const onBarClick = (event: MouseEvent) => {
  if (isInteractive(event.target)) {
    return
  }
  goHome()
}
</script>

<style scoped>
/* 纯黑背景：动态壁纸与叠加遮罩已移除，参考站也是纯黑底 */
.basic-layout {
  background: #000;
  position: relative;
  min-height: 100vh;
}

.main-content {
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
  /* 页脚改成多行合规信息后高度不再固定，这里用 flex:1 把页脚顶到底部，
     不再写 calc(100vh - 160px) 这种写死高度的魔法值 */
  flex: 1 1 auto;
}
</style>
