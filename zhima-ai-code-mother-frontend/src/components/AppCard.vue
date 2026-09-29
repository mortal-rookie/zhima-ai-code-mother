<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span class="placeholder-window"><i></i><i></i><i></i></span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button v-if="canViewChat" type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <!-- 头像可点击：打开作者信息弹窗 -->
        <span
          class="author-hit author-hit--avatar"
          role="button"
          tabindex="0"
          :title="app.user ? '查看作者信息' : ''"
          @click.stop="handleViewAuthor"
          @keydown.enter.stop="handleViewAuthor"
        >
          <a-avatar :src="app.user?.userAvatar" :size="40">
            {{ app.user?.userName?.charAt(0) || 'U' }}
          </a-avatar>
        </span>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <!-- 昵称同样可点 -->
        <p
          class="app-author author-hit"
          :title="app.user ? '查看作者信息' : ''"
          @click.stop="handleViewAuthor"
        >
          {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser'
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
  (e: 'view-author', user: API.UserVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()
const loginStore = useLoginUserStore()
const canViewChat = computed(
  () =>
    !!loginStore.loginUser.id &&
    (String(props.app.userId) === String(loginStore.loginUser.id) ||
      loginStore.loginUser.userRole === 'admin'),
)

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}

// 点作者头像/昵称 → 通知父组件弹出作者信息（作者信息随列表接口一起返回，无需额外请求）
const handleViewAuthor = () => {
  if (!props.app.user) {
    return
  }
  emit('view-author', props.app.user)
}
</script>

<style scoped>
.app-card {
  background: rgba(255, 255, 255, 0.78);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: var(--shadow);
  border: 1px solid var(--line);
  transition:
    transform 0.28s ease,
    box-shadow 0.28s ease,
    border-color 0.28s ease;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-2px);
  border-color: #b7cae3;
  box-shadow: 0 15px 35px rgba(58, 83, 115, 0.09);
}

.app-card--featured {
  border-color: var(--line);
}

.app-preview {
  height: 168px;
  background: linear-gradient(145deg, #e2edf5 0%, #f6eee4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-placeholder {
  font-size: 42px;
  color: #96b4d5;
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.62);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.app-card:hover .app-overlay,
.app-card:focus-within .app-overlay {
  opacity: 1;
}

.app-info {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.6);
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-author {
  font-size: 13px;
  color: #8e8e94;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ---------- 可点击的作者区 ---------- */
.author-hit {
  cursor: pointer;
  transition:
    color 0.2s ease,
    box-shadow 0.2s ease;
}

/* 头像：hover 时套一圈主色描边 */
.author-hit--avatar {
  display: inline-flex;
  border-radius: 50%;
}

.author-hit--avatar:hover,
.author-hit--avatar:focus-visible {
  box-shadow: 0 0 0 2px #b7cae3;
  outline: none;
}

/* 昵称：hover 时变主色 */
.app-author.author-hit:hover {
  color: var(--blue);
}
.placeholder-window {
  display: block;
  width: 90px;
  padding: 20px 14px;
  border: 1px solid white;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 8px 12px 20px rgba(60, 100, 145, 0.1);
}
.placeholder-window i {
  display: block;
  height: 5px;
  border-radius: 8px;
  background: #b4cce1;
  margin: 8px 0;
}
.placeholder-window i:nth-child(2) {
  width: 60%;
}
@media (hover: none) {
  .app-overlay {
    position: static;
    opacity: 1;
    padding: 12px;
    background: rgba(255, 255, 255, 0.7);
  }
  .app-preview {
    height: auto;
    min-height: 168px;
    flex-direction: column;
  }
  .app-preview > img {
    height: 168px;
  }
}
</style>
