<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700;900&family=Orbitron:wght@600;700&display=swap');

:root {
  /* ---------------------------------------------------------------
   * 以下三个遮罩变量属于「动态壁纸」时代，现在已经不再使用：
   * 背景改成纯黑、视频已移除，页面明暗不再需要遮罩控制。
   * 保留定义只是以防将来想恢复壁纸。
   * --------------------------------------------------------------- */
  --ld-veil-top: 0.44;
  --ld-veil-mid: 0.54;
  --ld-veil-bottom: 0.64;

  /* 白色雾面：把暗部整体抬一档，避免遮罩变淡后出现死黑。
     由上面三个值推导，遮罩越淡白雾越少，不会过曝，一般不用改 */
  --ld-haze: rgba(
    255,
    255,
    255,
    calc((var(--ld-veil-top) - 0.10) * 0.16)
  );

  /* ---------------------------------------------------------------
   * 配色严格对齐参考站 https://www.ldmnq.com
   *   主色 yellow-5 = #FFDB18，悬停/渐变落点 orange = #E27000
   *   辅助：浅黄 #FFF4C5、深黄 #E8A600
   *   底色：纯黑 #000，面板 #141414，分割线 rgba(255,255,255,.08)
   * 改这里就能全站换色（按钮、链接、分页都读这些变量）
   * --------------------------------------------------------------- */
  --ld-bg: #000000;
  --ld-bg-elevated: #141414;
  --ld-panel: rgba(20, 20, 20, 0.78);
  --ld-border: rgba(255, 255, 255, 0.08);
  --ld-text: #ffffff;
  --ld-text-muted: #9a9aa0;
  --ld-yellow: #ffdb18;
  --ld-yellow-soft: #fff4c5;
  --ld-yellow-deep: #e8a600;
  --ld-orange: #e27000;
  --ld-accent: linear-gradient(180deg, #ffe56a 0%, #ffdb18 52%, #e27000 100%);
  --ld-glow: 0 10px 28px rgba(255, 219, 24, 0.28);
  --ld-font: 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --ld-display: 'Orbitron', 'Noto Sans SC', sans-serif;
}

* {
  box-sizing: border-box;
}

html,
body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  background: var(--ld-bg);
  color: var(--ld-text);
  font-family: var(--ld-font);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  color: var(--ld-yellow);
}

/* Ant Design 主按钮：统一成纯色 #FFDB18 + 黑字
   与主页导航卡、参考站 .button-yellow 保持一致。
   之前这里用的是黄→橙渐变（--ld-accent），所以"搜索"这类按钮和导航卡放在一起颜色对不上。 */
.ant-btn-primary {
  background: #ffdb18 !important;
  border-color: #ffdb18 !important;
  color: #000000 !important;
  font-weight: 700 !important;
  box-shadow: 0 8px 22px rgba(255, 219, 24, 0.28);
  text-shadow: none !important;
}

/* 悬停：渐变到橙（沿用参考站的 .hover:to-#E27000） */
.ant-btn-primary:not(:disabled):hover {
  background: linear-gradient(135deg, #ffdb18 0%, #e27000 100%) !important;
  border-color: #e27000 !important;
  color: #1a1a1a !important;
}

.ant-btn-primary:disabled,
.ant-btn-primary.ant-btn-loading {
  opacity: 0.75;
}

.ant-pagination .ant-pagination-item-active {
  border-color: var(--ld-yellow) !important;
  background: rgba(255, 219, 24, 0.15) !important;
}

.ant-pagination .ant-pagination-item-active a {
  color: var(--ld-yellow) !important;
}

.ant-pagination .ant-pagination-item a,
.ant-pagination .ant-pagination-total-text,
.ant-pagination .ant-pagination-item-link {
  color: var(--ld-text-muted) !important;
}
</style>
