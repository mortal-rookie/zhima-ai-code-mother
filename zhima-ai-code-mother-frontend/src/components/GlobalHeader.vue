<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">峙码应用生成平台</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="go('/my/apps')">
                    <AppstoreOutlined />
                    我的应用
                  </a-menu-item>
                  <a-menu-item @click="go('/user/profile')">
                    <UserOutlined />
                    个人资料
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined, AppstoreOutlined, UserOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/my/apps',
    icon: () => h(AppstoreOutlined),
    label: '我的应用',
    title: '我的应用',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 用户下拉菜单里的跳转
const go = (path: string) => {
  if (path.startsWith('/')) {
    router.push(path)
  }
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: rgba(0, 0, 0, 0.72);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  padding: 0 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  line-height: 64px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 40px;
  width: 40px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 0 0 2px rgba(255, 219, 24, 0.45);
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #fff;
  background: linear-gradient(90deg, #fff4c5 0%, #ffdb18 45%, #ffffff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.user-login-status {
  color: #f5f5f5;
  display: flex;
  align-items: center;
  height: 64px;
}

.ant-menu-horizontal {
  border-bottom: none !important;
  background: transparent !important;
  /* 原来是 62px 的行高，现在改成 flex 居中，让卡片按钮在 64px 的头部里垂直居中 */
  display: flex;
  align-items: center;
  height: 64px;
  line-height: normal;
  gap: 10px;
}

:deep(.ant-menu-light) {
  background: transparent;
  color: rgba(255, 255, 255, 0.78);
}

/* ==============================================================
   导航项 → 参考站那种「斜切卡片」按钮
   形状直接取自参考站的 SVG 路径（viewBox 277x80）：左右两边都是斜的、
   四角带圆角，所以不能只用 border-radius 糊出来。
   这里把它做成可拉伸的 data-uri 背景，background-size:100% 100% 保证
   任意尺寸下形状都不变形。
   配色同样照抄参考站：
     默认  c-white c-op-12  → 白 12% 叠加（纯黑底上约等于 #1f1f1f）
     悬停  c-op-24          → 白 24%
     选中  c-yellow-5       → #FFDB18，文字转黑
   ============================================================== */
:deep(.ant-menu-horizontal > .ant-menu-item),
:deep(.ant-menu-horizontal > .ant-menu-submenu) {
  height: 40px;
  line-height: 40px;
  margin: 0 !important;
  /* 右边斜边会吃掉一点空间，所以左右内边距给得不一样 */
  padding: 0 30px 0 26px !important;
  border-radius: 0;
  background-color: transparent;
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 277 80' preserveAspectRatio='none'%3E%3Cpath d='M264.134 0C271.471 0.000116906 277.09 6.52509 276.001 13.7803L267.997 67.1436C266.888 74.5331 260.54 80 253.068 80H12.0024C4.66609 79.9999 -0.953027 73.4749 0.135254 66.2197L8.14012 12.8564C9.24854 5.46708 15.5959 0.000224657 23.0679 0H264.134Z' fill='%2332353a'/%3E%3C/svg%3E");
  color: #ffffff !important;
  font-weight: 700;
  letter-spacing: 0.02em;
  transition:
    color 0.2s ease,
    background-image 0.2s ease;
}

:deep(.ant-menu-horizontal > .ant-menu-item:hover),
:deep(.ant-menu-horizontal > .ant-menu-submenu:hover) {
  background-color: transparent !important;
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 277 80' preserveAspectRatio='none'%3E%3Cpath d='M264.134 0C271.471 0.000116906 277.09 6.52509 276.001 13.7803L267.997 67.1436C266.888 74.5331 260.54 80 253.068 80H12.0024C4.66609 79.9999 -0.953027 73.4749 0.135254 66.2197L8.14012 12.8564C9.24854 5.46708 15.5959 0.000224657 23.0679 0H264.134Z' fill='%2341454c'/%3E%3C/svg%3E");
  color: #ffffff !important;
}

/* 选中：黄底黑字（参考站的 group-data-[active=true] 状态） */
:deep(.ant-menu-horizontal > .ant-menu-item-selected) {
  /* antd 默认会给选中项铺一层浅色底，必须显式压掉，
     否则它会透在黄色卡片下面，颜色就不准了 */
  background-color: transparent !important;
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 277 80' preserveAspectRatio='none'%3E%3Cpath d='M264.134 0C271.471 0.000116906 277.09 6.52509 276.001 13.7803L267.997 67.1436C266.888 74.5331 260.54 80 253.068 80H12.0024C4.66609 79.9999 -0.953027 73.4749 0.135254 66.2197L8.14012 12.8564C9.24854 5.46708 15.5959 0.000224657 23.0679 0H264.134Z' fill='%23ffdb18'/%3E%3C/svg%3E");
  color: #000000 !important;
}

/* 干掉 antd 默认那条底部指示下划线，形状已经由卡片本身表达了 */
:deep(.ant-menu-horizontal > .ant-menu-item::after),
:deep(.ant-menu-horizontal > .ant-menu-item-selected::after) {
  display: none !important;
  border-bottom: none !important;
}

/* 窄屏收紧内边距，避免四个卡片把头部挤爆 */
@media (max-width: 992px) {
  :deep(.ant-menu-horizontal > .ant-menu-item),
  :deep(.ant-menu-horizontal > .ant-menu-submenu) {
    padding: 0 18px 0 16px !important;
    font-size: 13px;
  }

  .ant-menu-horizontal {
    gap: 6px;
  }
}

:deep(.ant-btn-primary) {
  border-radius: 999px;
  height: 36px;
  padding: 0 20px;
}
</style>
