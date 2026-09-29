<template>
  <div id="userLoginPage">
    <h2 class="title">AI 应用生成 - 用户登录</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
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
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
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
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  background: rgba(14, 14, 16, 0.82);
  max-width: 480px;
  padding: 36px 32px 28px;
  margin: 64px auto;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}

.title {
  text-align: center;
  margin-bottom: 10px;
  color: #fff;
  font-weight: 700;
}

.desc {
  text-align: center;
  color: #9a9aa0;
  margin-bottom: 24px;
}

.tips {
  text-align: right;
  color: #7a7a80;
  font-size: 13px;
  margin-bottom: 16px;
}

.tips a {
  color: #ffdb18;
}

/* 登录即同意协议提示 */
.agreement-tip {
  margin: 14px 0 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.38);
  font-size: 12px;
  line-height: 1.8;
}

.agreement-tip a {
  color: #ffdb18;
}

:deep(.ant-input),
:deep(.ant-input-password),
:deep(.ant-input-affix-wrapper) {
  background: rgba(255, 255, 255, 0.04) !important;
  border-color: rgba(255, 255, 255, 0.12) !important;
  color: #f5f5f5 !important;
}

:deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.35) !important;
}

:deep(.ant-btn-primary) {
  height: 44px;
  border-radius: 999px;
  font-size: 16px;
}
</style>
