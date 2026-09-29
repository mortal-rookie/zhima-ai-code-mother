<script setup lang="ts">
import { computed, ref } from 'vue'
import { usePageMotion } from '@/utils/pageMotion'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
const route = useRoute()
const motionRoot = ref<HTMLElement>()
usePageMotion(motionRoot)
const landing = computed(() => route.path === '/welcome')
const chat = computed(() => route.path.startsWith('/app/chat/'))
const auth = computed(() => ['/user/login', '/user/register'].includes(route.path))
const sidebarCollapsed = ref(false)
try {
  sidebarCollapsed.value = localStorage.getItem('zhima-sidebar-collapsed') === 'true'
} catch {
  /* Layout also works when browser storage is unavailable. */
}
function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  try {
    localStorage.setItem('zhima-sidebar-collapsed', String(sidebarCollapsed.value))
  } catch {
    /* Keep the current session's selection. */
  }
}
</script>
<template>
  <div
    ref="motionRoot"
    class="basic-layout"
    :class="{ 'landing-layout': landing, 'sidebar-collapsed': sidebarCollapsed }"
  >
    <GlobalHeader v-if="!landing" :collapsed="sidebarCollapsed" @toggle="toggleSidebar" />
    <div class="workspace">
      <main id="main-content" :class="{ 'auth-content': auth }">
        <router-view v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" :key="route.path" />
          </Transition>
        </router-view>
      </main>
      <GlobalFooter v-if="!chat" />
    </div>
  </div>
</template>
<style scoped>
.basic-layout {
  min-height: 100dvh;
  background:
    radial-gradient(ellipse at 90% 0%, rgba(215, 230, 241, 0.6), transparent 42%),
    radial-gradient(ellipse at 0% 100%, rgba(249, 230, 213, 0.3), transparent 46%), #f8f7f5;
}
.workspace {
  margin-left: 208px;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  transition:
    margin-left 350ms var(--ease-soft),
    padding-top 350ms var(--ease-soft);
}
.sidebar-collapsed .workspace {
  margin-left: 80px;
}
main {
  flex: 1;
  min-width: 0;
}
.auth-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.landing-layout .workspace {
  margin-left: 0;
}
@media (max-width: 760px) {
  .auth-content {
    padding: 24px 16px;
  }
  .workspace {
    margin-left: 0;
    padding-top: 120px;
  }
  .sidebar-collapsed .workspace {
    margin-left: 0;
    padding-top: 64px;
  }
  .landing-layout .workspace {
    padding-top: 0;
  }
}
</style>
