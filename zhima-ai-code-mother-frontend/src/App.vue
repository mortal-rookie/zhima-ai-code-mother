<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import '@/assets/theme.css'
import '@/assets/motion.css'
const theme = {
  token: {
    colorPrimary: '#426be8',
    colorText: '#1c2937',
    colorTextSecondary: '#737e8b',
    colorBorder: '#e5e7eb',
    borderRadius: 12,
    fontFamily: '"Microsoft YaHei", "PingFang SC", sans-serif',
  },
}
</script>
<template>
  <a-config-provider :theme="theme"><BasicLayout /></a-config-provider>
</template>
