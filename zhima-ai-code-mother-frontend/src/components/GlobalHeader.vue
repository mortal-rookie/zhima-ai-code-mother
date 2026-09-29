<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  HomeOutlined,
  PlusCircleOutlined,
  AppstoreOutlined,
  FolderOutlined,
  BookOutlined,
  SettingOutlined,
  RightOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons-vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
defineProps<{ collapsed: boolean }>()
const emit = defineEmits<{ toggle: [] }>()
const store = useLoginUserStore()
const route = useRoute()
const router = useRouter()
const links = computed(() => [
  { path: '/', label: '首页', icon: HomeOutlined },
  { path: '/app/new', label: '新建', icon: PlusCircleOutlined },
  { path: '/templates', label: '模板', icon: AppstoreOutlined },
  { path: '/my/apps', label: '我的应用', icon: FolderOutlined },
  { path: '/resources', label: '资源库', icon: BookOutlined },
  ...(store.loginUser.userRole === 'admin'
    ? [
        { path: '/admin/userManage', label: '用户管理', icon: SettingOutlined },
        { path: '/admin/appManage', label: '应用管理', icon: AppstoreOutlined },
        { path: '/admin/chatManage', label: '对话管理', icon: BookOutlined },
      ]
    : []),
])
const active = (path: string) => (path === '/' ? route.path === '/' : route.path.startsWith(path))
async function logout() {
  try {
    const res = await userLogout()
    if (res.data.code !== 0) throw new Error(res.data.message || '退出失败')
    store.setLoginUser({ userName: '未登录' })
    await router.push('/')
    message.success('已退出登录')
  } catch {
    message.error('退出失败，请重试')
  }
}
</script>
<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="brand-row">
      <RouterLink to="/welcome" class="brand-link" aria-label="峙码品牌首页"
        ><BrandLogo
      /></RouterLink>
      <button
        class="sidebar-toggle"
        type="button"
        :aria-label="collapsed ? '展开侧边栏' : '收起侧边栏'"
        :title="collapsed ? '展开侧边栏' : '收起侧边栏'"
        :aria-expanded="!collapsed"
        aria-controls="sidebar-navigation"
        @click="emit('toggle')"
      >
        <MenuUnfoldOutlined v-if="collapsed" /><MenuFoldOutlined v-else />
      </button>
    </div>
    <nav id="sidebar-navigation" aria-label="主导航">
      <RouterLink
        v-for="link in links"
        :key="link.path"
        :to="link.path"
        class="nav-link"
        :class="{ active: active(link.path) }"
        :aria-current="active(link.path) ? 'page' : undefined"
        :aria-label="link.label"
        :title="collapsed ? link.label : undefined"
        ><component :is="link.icon" /><span>{{ link.label }}</span></RouterLink
      >
    </nav>
    <div class="sidebar-bottom">
      <a-dropdown v-if="store.loginUser.id" :trigger="['click']" placement="topLeft"
        ><button
          class="profile-button"
          aria-label="个人空间"
          :title="collapsed ? '个人空间' : undefined"
        >
          <a-avatar :size="30" :src="store.loginUser.userAvatar" class="profile-avatar">{{
            store.loginUser.userName?.slice(0, 1)
          }}</a-avatar
          ><span>个人空间</span><RightOutlined /></button
        ><template #overlay
          ><a-menu
            ><a-menu-item @click="router.push('/user/profile')">个人资料</a-menu-item
            ><a-menu-item @click="router.push('/my/apps')">我的应用</a-menu-item
            ><a-menu-divider /><a-menu-item @click="logout"
              ><LogoutOutlined /> 退出登录</a-menu-item
            ></a-menu
          ></template
        ></a-dropdown
      ><RouterLink
        v-else
        class="profile-button"
        to="/user/login"
        aria-label="登录 / 注册"
        :title="collapsed ? '登录 / 注册' : undefined"
        ><span class="guest-avatar"></span><span>登录 / 注册</span><RightOutlined
      /></RouterLink>
    </div>
  </aside>
</template>
<style scoped>
.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 208px;
  padding: 38px 24px 26px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(40, 55, 75, 0.06);
  background: rgba(249, 248, 246, 0.78);
  backdrop-filter: blur(24px);
  transition:
    width 350ms var(--ease-soft),
    padding 350ms var(--ease-soft),
    height 350ms var(--ease-soft);
}
.brand-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 48px;
}
.brand-link {
  margin-left: 8px;
}
.sidebar-toggle {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: rgba(255, 255, 255, 0.5);
  color: #6b7c90;
  transition:
    background-color 200ms ease,
    transform 300ms var(--ease-spring);
}
.sidebar-toggle:hover {
  background: #e8eef7;
  color: var(--blue);
}
.sidebar-toggle:active {
  transform: scale(0.92);
}
@media (min-width: 761px) {
  .sidebar.collapsed {
    width: 80px;
    padding-left: 16px;
    padding-right: 16px;
  }
  .collapsed .brand-row {
    flex-direction: column;
    gap: 20px;
    margin-bottom: 28px;
  }
  .collapsed .brand-link {
    margin-left: 0;
  }
  .collapsed :deep(.brand > span) {
    display: none;
  }
  .collapsed .nav-link {
    justify-content: center;
    padding: 0;
    gap: 0;
  }
  .collapsed .nav-link > span:not(.anticon) {
    display: none;
  }
  .collapsed .profile-button {
    justify-content: center;
    padding: 8px 0;
    gap: 0;
  }
  .collapsed .profile-button > span:not(.profile-avatar):not(.guest-avatar),
  .collapsed .profile-button > .anticon {
    display: none;
  }
}
nav {
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: 43px;
  padding: 0 16px;
  color: #505a66;
  border-radius: 11px;
  font-size: 14px;
}
.nav-link .anticon {
  font-size: 16px;
}
.nav-link:hover {
  background: rgba(220, 230, 240, 0.32);
}
.nav-link.active {
  background: #e9edf0;
  color: #23384c;
}
.nav-link.active .anticon {
  color: #315fed;
}
.sidebar-bottom {
  margin-top: auto;
  padding-top: 30px;
}
.profile-button {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px;
  gap: 12px;
  color: #64707e;
  background: none;
  border: 0;
  font-size: 13px;
}
.profile-button > .anticon {
  margin-left: auto;
  font-size: 11px;
}
.profile-avatar,
.guest-avatar {
  background: linear-gradient(135deg, #ece0cf, #a9c3d8);
}
.guest-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}
@media (max-width: 760px) {
  .brand-row {
    margin-bottom: 0;
  }
  .sidebar-toggle {
    position: absolute;
    top: 19px;
    right: 18px;
  }
  .sidebar.collapsed {
    height: 64px;
  }
  .collapsed nav {
    display: none;
  }
  .sidebar {
    width: 100%;
    height: 120px;
    padding: 16px 20px 10px;
    bottom: auto;
    border-right: 0;
    border-bottom: 1px solid var(--line);
  }
  .brand-link {
    margin: 0;
  }
  nav {
    flex-direction: row;
    gap: 4px;
    overflow-x: auto;
    scrollbar-width: none;
    margin-top: 16px;
  }
  nav::-webkit-scrollbar {
    display: none;
  }
  .nav-link {
    flex-shrink: 0;
    min-height: 36px;
    padding: 0 10px;
    gap: 6px;
    font-size: 12px;
  }
  .sidebar-bottom {
    position: absolute;
    right: 62px;
    top: 12px;
    margin: 0;
    padding: 0;
  }
  .profile-button {
    width: auto;
  }
}
</style>
