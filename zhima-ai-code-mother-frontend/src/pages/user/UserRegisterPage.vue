<template>
  <div id="userRegisterPage">
    <h2 class="title">开启你的创作空间</h2>
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
        <RouterLink :to="{ path: '/user/login', query: { redirect: route.query.redirect } }"
          >去登录</RouterLink
        >
      </div>
      <a-form-item
        name="agreement"
        :rules="[{ validator: validateAgreement }]"
        class="agreement-item"
      >
        <a-checkbox v-model:checked="formState.agreement">
          我已阅读并同意
          <RouterLink to="/user/agreement" target="_blank">《用户协议》</RouterLink>
          和
          <RouterLink to="/user/privacy" target="_blank">《隐私协议》</RouterLink>
        </a-checkbox>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="submitting" style="width: 100%"
          >注册</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)

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
  if (submitting.value) return
  submitting.value = true
  try {
    // agreement 只是前端勾选状态，不发给后端
    const res = await userRegister({
      userAccount: values.userAccount,
      userPassword: values.userPassword,
      checkPassword: values.checkPassword,
    })
    // 注册成功，跳转到登录页面
    if (res.data.code === 0) {
      message.success('注册成功')
      await router.replace({
        path: '/user/login',
        query: { redirect: route.query.redirect },
      })
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '注册失败，请重试')
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
