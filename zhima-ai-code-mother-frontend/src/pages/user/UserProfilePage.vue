<template>
  <div id="userProfilePage">
    <a-card :bordered="false" class="profile-card">
      <h2 class="page-title">个人资料</h2>

      <a-row :gutter="32">
        <!-- 左侧：资料预览 -->
        <a-col :xs="24" :md="8">
          <div class="preview">
            <a-avatar :size="112" :src="form.userAvatar">
              {{ (form.userName || '无').slice(0, 1) }}
            </a-avatar>
            <div class="preview-name">{{ form.userName || '未设置昵称' }}</div>
            <a-tag :color="isAdmin ? 'green' : 'blue'">
              {{ isAdmin ? '管理员' : '普通用户' }}
            </a-tag>
            <div class="preview-account">账号：{{ loginUser.userAccount || '-' }}</div>

            <!-- 今日生成次数：普通用户每天有固定免费次数，管理员不受限制 -->
            <div class="preview-quota">
              <div class="quota-label">今日生成次数</div>
              <div class="quota-value" :class="{ 'is-empty': genUsedUp }">
                <template v-if="isAdmin">不限次数</template>
                <template v-else>
                  {{ todayGenUsed }}<span class="quota-total"> / {{ DAILY_FREE_GEN_QUOTA }}</span>
                </template>
              </div>
              <div class="quota-note" :class="{ 'is-empty': genUsedUp }">
                <template v-if="isAdmin">管理员不受每日次数限制</template>
                <template v-else-if="genUsedUp">今日免费次数已用光，请明天再来</template>
                <template v-else>今日剩余 {{ genRemaining }} 次，每日 0 点重置</template>
              </div>
            </div>

            <div class="preview-profile">{{ form.userProfile || '这个人很懒，什么都没写' }}</div>
          </div>
        </a-col>

        <!-- 右侧：编辑表单 -->
        <a-col :xs="24" :md="16">
          <a-form :model="form" :label-col="{ span: 4 }" :wrapper-col="{ span: 19 }">
            <a-form-item label="账号">
              <a-input :value="loginUser.userAccount" disabled />
              <div class="form-tip">账号是登录凭据，创建后不可修改</div>
            </a-form-item>

            <a-form-item label="昵称">
              <a-input v-model:value="form.userName" placeholder="请输入昵称" allow-clear />
            </a-form-item>

            <a-form-item label="头像">
              <div class="avatar-uploader">
                <a-upload
                  name="file"
                  list-type="picture-card"
                  :show-upload-list="false"
                  :max-count="1"
                  :before-upload="beforeAvatarUpload"
                  :custom-request="customUploadAvatar"
                  :disabled="uploading"
                  accept="image/jpeg,image/png,image/webp,image/gif"
                >
                  <img v-if="form.userAvatar" :src="form.userAvatar" class="avatar-thumb" alt="头像" />
                  <div v-else class="avatar-placeholder">
                    <PlusOutlined />
                    <div>上传头像</div>
                  </div>
                </a-upload>
                <div class="avatar-uploader-tip">
                  支持 jpg / png / webp / gif，不超过 5MB。
                  <br />
                  上传后只是填进表单，还要点下方「保存修改」才会生效。
                </div>
              </div>
              <a-input
                v-model:value="form.userAvatar"
                placeholder="也可以直接粘贴图片地址"
                allow-clear
                style="margin-top: 10px"
              />
            </a-form-item>

            <a-form-item label="简介">
              <a-textarea
                v-model:value="form.userProfile"
                :rows="4"
                placeholder="介绍一下自己"
                show-count
                :maxlength="200"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 4, span: 19 }">
              <a-space>
                <a-button type="primary" :loading="saving" @click="doSave">保存修改</a-button>
                <a-button @click="resetForm">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { isAxiosError } from 'axios'
import { type UploadProps, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { updateMyUser, updateUser, uploadAvatar } from '@/api/userController'
import { DAILY_FREE_GEN_QUOTA, resolveTodayGenUsed } from '@/config/genQuota.ts'

const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 今日生成次数：后端只给了「次数 + 这个次数对应的日期」，
// 跨天归零的判断放在 @/config/genQuota 里统一处理
const todayGenUsed = computed(() => resolveTodayGenUsed(loginUser.value))
const genRemaining = computed(() => Math.max(0, DAILY_FREE_GEN_QUOTA - todayGenUsed.value))
// 次数用光（管理员不受限制，永远不算用光）
const genUsedUp = computed(() => !isAdmin.value && genRemaining.value === 0)

const saving = ref(false)

const form = reactive<API.UserUpdateMyRequest>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const resetForm = () => {
  form.userName = loginUser.value.userName ?? ''
  form.userAvatar = loginUser.value.userAvatar ?? ''
  form.userProfile = loginUser.value.userProfile ?? ''
}

// ===== 头像上传 =====
const uploading = ref(false)

// 上传前先校验：类型 + 大小。和后端保持一致，避免白传一趟再被拒
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
  if (!allowed.includes(file.type)) {
    message.error('只能上传 jpg / png / webp / gif 图片')
    return false
  }
  if (file.size / 1024 / 1024 >= 5) {
    message.error('头像不能超过 5MB')
    return false
  }
  return true
}

// 用 custom-request 接管：a-upload 默认会自己往 action 提交，不接管的话文件根本不会发到我们的接口
const customUploadAvatar: UploadProps['customRequest'] = async (options) => {
  const formData = new FormData()
  // 字段名必须是 file，和后端 @RequestParam("file") 对齐
  formData.append('file', options.file as File)
  uploading.value = true
  try {
    const res = await uploadAvatar(formData)
    if (res.data.code === 0 && res.data.data) {
      // 拿到 COS 地址后只填进表单，由用户点「保存修改」再落库
      form.userAvatar = res.data.data
      options.onSuccess?.(res.data)
      message.success('头像上传成功，记得点「保存修改」')
    } else {
      options.onError?.(new Error(res.data.message))
      message.error('头像上传失败：' + res.data.message)
    }
  } catch (error) {
    options.onError?.(error as Error)
    console.error('头像上传失败：', error)
    message.error('头像上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const doSave = async () => {
  if (!form.userName && !form.userAvatar && !form.userProfile) {
    message.warning('请至少填写一项要修改的内容')
    return
  }
  saving.value = true
  try {
    let res
    try {
      res = await updateMyUser({ ...form })
    } catch (error) {
      // 后端还没有实现 /user/update/my 时会返回 404。
      // 管理员可以临时退回用管理员接口改自己的资料（只传昵称/头像/简介，不带 userRole，不会提权）；
      // 普通用户没有可用的接口，只能等后端补上。
      const notImplemented = isAxiosError(error) && error.response?.status === 404
      if (notImplemented && isAdmin.value && loginUser.value.id) {
        console.warn(
          '[临时兼容] 后端尚未实现 POST /user/update/my，管理员账号回退到 POST /user/update',
        )
        res = await updateUser({
          id: loginUser.value.id,
          userName: form.userName,
          userAvatar: form.userAvatar,
          userProfile: form.userProfile,
        })
      } else {
        throw error
      }
    }

    if (res.data.code === 0) {
      message.success('保存成功')
      // 重新拉一次登录用户信息，让顶部头像和昵称立刻同步
      await loginUserStore.fetchLoginUser()
      resetForm()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    console.error('保存个人资料失败：', error)
    if (isAxiosError(error) && error.response?.status === 404) {
      message.error('后端还没有实现 POST /user/update/my 接口，暂时无法保存（管理员账号会自动回退）')
    } else {
      message.error('保存失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  // 直接刷新页面进来时，store 里可能还是默认值
  await loginUserStore.fetchLoginUser()
  resetForm()
})
</script>

<style scoped>
#userProfilePage {
  max-width: 1000px;
  margin: 16px auto;
}

.profile-card {
  border-radius: 12px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 600;
}

.preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 0 24px;
  border-right: 1px solid #f0f0f0;
  text-align: center;
}

.preview-name {
  font-size: 17px;
  font-weight: 600;
}

.preview-account {
  color: #8c8c8c;
  font-size: 13px;
}

/* 今日生成次数 */
.preview-quota {
  width: 100%;
  max-width: 240px;
  margin-top: 6px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f7f8fa;
  border: 1px solid #f0f0f0;
}

.quota-label {
  color: #8c8c8c;
  font-size: 12px;
}

.quota-value {
  color: #1a1a1a;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.4;
}

/* 次数用光时标红，比灰色更醒目 */
.quota-value.is-empty {
  color: #d4380d;
}

.quota-total {
  color: #8c8c8c;
  font-size: 14px;
  font-weight: 500;
}

.quota-note {
  color: #8c8c8c;
  font-size: 12px;
  line-height: 1.6;
}

/* 次数用光时提示也标红，和上面的数字呼应 */
.quota-note.is-empty {
  color: #d4380d;
}

.preview-profile {
  color: #595959;
  font-size: 13px;
  line-height: 1.7;
  max-width: 240px;
}

.form-tip {
  color: #8c8c8c;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}

.avatar-uploader {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.avatar-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #8c8c8c;
}

.avatar-uploader-tip {
  color: #8c8c8c;
  font-size: 12px;
  line-height: 1.8;
  padding-top: 4px;
}

@media (max-width: 768px) {
  .preview {
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }
}
</style>
