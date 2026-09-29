import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import WelcomePage from '@/pages/WelcomePage.vue'
import TemplatesPage from '@/pages/TemplatesPage.vue'
import NewAppPage from '@/pages/app/NewAppPage.vue'
import ResourcesPage from '@/pages/ResourcesPage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import MyAppsPage from '@/pages/app/MyAppsPage.vue'
import UserProfilePage from '@/pages/user/UserProfilePage.vue'
import UserAgreementPage from '@/pages/user/UserAgreementPage.vue'
import PrivacyPolicyPage from '@/pages/user/PrivacyPolicyPage.vue'
import ChatManagePage from '@/pages/admin/ChatManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/welcome', name: '品牌首页', component: WelcomePage },
    { path: '/templates', name: '需求模板', component: TemplatesPage },
    { path: '/app/new', name: '新建应用', component: NewAppPage },
    { path: '/resources', name: '资源库', component: ResourcesPage },
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/admin/chatManage',
      name: '对话管理',
      component: ChatManagePage,
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: AppEditPage,
    },
    {
      path: '/my/apps',
      name: '我的应用',
      component: MyAppsPage,
    },
    {
      path: '/user/profile',
      name: '个人资料',
      component: UserProfilePage,
    },
    {
      path: '/user/agreement',
      name: '用户协议',
      component: UserAgreementPage,
    },
    {
      path: '/user/privacy',
      name: '隐私协议',
      component: PrivacyPolicyPage,
    },
  ],
})

export default router
