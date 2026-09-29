<template>
  <div id="userRegisterPage">
    <h2 class="title">AI 应用生成 - 用户注册</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '密码不能小于 8 位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
      </a-form-item>
      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item name="agreement" :rules="[{ validator: validateAgreement }]" class="agreement-item">
        <a-checkbox v-model:checked="formState.agreement">
          我已阅读并同意
          <RouterLink to="/user/agreement" target="_blank">《用户协议》</RouterLink>
          和
          <RouterLink to="/user/privacy" target="_blank">《隐私协议》</RouterLink>
        </a-checkbox>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest & { agreement: boolean }>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  agreement: false,
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 验证是否勾选了用户协议与隐私协议：
 * 没勾选直接报错，a-form 校验不通过就不会触发 @finish，也就拦住提交了
 */
const validateAgreement = (rule: unknown, value: boolean, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请先阅读并同意《用户协议》和《隐私协议》'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest & { agreement: boolean }) => {
  // agreement 只是前端勾选状态，不发给后端
  const res = await userRegister({
    userAccount: values.userAccount,
    userPassword: values.userPassword,
    checkPassword: values.checkPassword,
  })
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
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
  margin-bottom: 16px;
  color: #7a7a80;
  font-size: 13px;
  text-align: right;
}

.tips a {
  color: #ffdb18;
}

/* 协议勾选框 */
.agreement-item {
  margin-bottom: 16px;
}

.agreement-item :deep(.ant-checkbox-wrapper) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  line-height: 1.7;
  align-items: flex-start;
}

.agreement-item :deep(.ant-checkbox) {
  margin-top: 3px;
}

.agreement-item :deep(.ant-checkbox-wrapper a) {
  color: #ffdb18;
}

.agreement-item :deep(.ant-form-item-explain-error) {
  font-size: 12px;
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
