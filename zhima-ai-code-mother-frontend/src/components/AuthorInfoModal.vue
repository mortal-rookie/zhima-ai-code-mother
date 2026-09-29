<template>
  <a-modal v-model:open="visible" title="作者信息" :footer="null" width="400px">
    <div class="author-info">
      <a-avatar :size="76" :src="user?.userAvatar">
        {{ user?.userName?.charAt(0) || 'U' }}
      </a-avatar>

      <div class="author-name">{{ user?.userName || '未知用户' }}</div>

      <a-tag :color="isAdmin ? 'gold' : 'blue'">
        {{ isAdmin ? '管理员' : '普通用户' }}
      </a-tag>

      <p class="author-profile">{{ user?.userProfile || '这个人很懒，什么都没写' }}</p>

      <div class="author-meta">加入时间：{{ formatTime(user?.createTime, 'YYYY-MM-DD') || '-' }}</div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatTime } from '@/utils/time'

/**
 * 作者信息弹窗。
 *
 * 数据来源就是列表接口里 AppVO.user 那一份（后端 getUserVOList 已经把作者塞进去了），
 * 所以打开这个弹窗不需要任何额外请求。
 * 注意：这里只展示昵称/头像/简介/注册时间，故意不展示 userAccount ——
 * 账号是登录凭据，露出去等于把用户名送给撞库脚本。
 */
interface Props {
  open: boolean
  user?: API.UserVO
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const isAdmin = computed(() => props.user?.userRole === 'admin')
</script>

<style scoped>
.author-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 8px 0 4px;
}

.author-name {
  margin-top: 14px;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}

.author-info :deep(.ant-tag) {
  margin-top: 10px;
  margin-inline-end: 0;
}

.author-profile {
  margin: 16px 0 0;
  max-width: 300px;
  color: #595959;
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.author-meta {
  margin-top: 14px;
  padding-top: 12px;
  width: 100%;
  border-top: 1px solid #f0f0f0;
  color: #8c8c8c;
  font-size: 12px;
}
</style>
