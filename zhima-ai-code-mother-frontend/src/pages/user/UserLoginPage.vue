<template>
  <div id="userLoginPage">
    <h2 class="title">欢迎回到峙码</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        name="userAccount"
        :rules="[
          { required: true, message: '请输入账号' },
          { min: 4, message: '账号至少 4 位' },
        ]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号
        <RouterLink :to="{ path: '/user/register', query: { redirect: route.query.redirect } }"
          >去注册</RouterLink
        >
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="submitting" style="width: 100%"
          >登录</a-button
        >
      </a-form-item>
      <p class="agreement-tip">
        登录即代表您已阅读并同意
        <RouterLink to="/user/agreement" target="_blank">《用户协议》</RouterLink>
        和
        <RouterLink to="/user/privacy" target="_blank">《隐私协议》</RouterLink>
      </p>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserLoginRequest) => {
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await userLogin(values)
    if (res.data.code !== 0 || !res.data.data) throw new Error(res.data.message || '登录失败')
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    const target = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    // Restrict redirects to this application, including legacy absolute same-origin redirects.
    let destination = '/'
    try {
      const url = new URL(target, window.location.origin)
      if (url.origin === window.location.origin) destination = url.pathname + url.search + url.hash
    } catch {
      /* use homepage */
    }
    await router.replace(destination)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '登录失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userLoginPage,
#userRegisterPage {
  background: rgba(255, 255, 255, 0.75);
  max-width: 460px;
  width: 100%;
  padding: 42px 36px 30px;
  margin: 0;
  border-radius: 24px;
  border: 1px solid white;
  backdrop-filter: blur(18px);
  box-shadow: var(--shadow);
}
.title {
  font-family: var(--display);
  text-align: center;
  margin: 0 0 12px;
  color: var(--ink);
  font-weight: 500;
  font-size: 30px;
}
.desc {
  text-align: center;
  color: var(--muted);
  margin-bottom: 32px;
  font-size: 13px;
}
.tips {
  text-align: right;
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 20px;
}
.agreement-tip {
  margin: 18px 0 0;
  text-align: center;
  color: var(--muted);
  font-size: 11px;
  line-height: 1.9;
}
.agreement-item :deep(.ant-checkbox-wrapper) {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.8;
}
:deep(.ant-input-affix-wrapper),
:deep(.ant-input:not(.ant-input-affix-wrapper input)) {
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.8);
}
:deep(.ant-btn-primary) {
  height: 46px;
  border-radius: 999px;
  background: var(--ink);
  border-color: var(--ink);
  font-size: 14px;
}
@media (max-width: 640px) {
  #userLoginPage,
  #userRegisterPage {
    padding: 32px 24px;
  }
}
</style>
