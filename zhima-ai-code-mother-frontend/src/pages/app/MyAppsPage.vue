<template>
  <div id="myAppsPage">
    <a-card :bordered="false" class="main-card">
      <div class="page-head">
        <div>
          <h2 class="page-title">我的应用</h2>
        </div>
        <a-button type="primary" @click="openCreateModal">+ 新建应用</a-button>
      </div>

      <!-- 搜索 -->
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="应用名称">
          <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" allow-clear />
        </a-form-item>
        <a-form-item label="生成类型">
          <a-select
            v-model:value="searchParams.codeGenType"
            placeholder="全部"
            style="width: 160px"
            allow-clear
          >
            <a-select-option
              v-for="option in CODE_GEN_TYPE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="doResetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        :scroll="{ x: 1140 }"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'cover'">
            <a-image v-if="record.cover" :src="record.cover" :width="80" :height="60" />
            <div v-else class="no-cover">无封面</div>
          </template>
          <template v-else-if="column.dataIndex === 'initPrompt'">
            <a-tooltip :title="record.initPrompt">
              <div class="prompt-text">{{ record.initPrompt }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'codeGenType'">
            <a-tag color="purple">{{ formatCodeGenType(record.codeGenType) }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'deployedTime'">
            <a-tag v-if="record.deployKey" color="green">已部署</a-tag>
            <span v-else class="text-gray">未部署</span>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space :size="4">
              <a-button type="link" size="small" @click="goChat(record)">对话</a-button>
              <a-button type="link" size="small" @click="openPreview(record)">预览</a-button>
              <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="openRenameModal(record)">重命名</a-button>
              <a-popconfirm
                title="删除后该应用及其对话记录将不可恢复，确定删除吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="doDelete(record.id)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
        <template #emptyText>
          <a-empty description="还没有应用，点右上角「新建应用」开始吧" />
        </template>
      </a-table>
    </a-card>

    <!-- 新建应用 -->
    <a-modal
      v-model:open="createVisible"
      title="新建应用"
      :confirm-loading="creating"
      ok-text="创建并开始生成"
      @ok="doCreate"
    >
      <a-form layout="vertical">
        <a-form-item label="描述你想要的网站" required>
          <a-textarea
            v-model:value="createForm.initPrompt"
            :rows="5"
            placeholder="例如：创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能"
            show-count
            :maxlength="500"
          />
        </a-form-item>
      </a-form>
      <a-alert
        type="info"
        show-icon
        message="创建后会自动跳转到对话页并开始生成，生成过程会消耗 AI token"
      />
    </a-modal>

    <!-- 重命名 -->
    <a-modal
      v-model:open="renameVisible"
      title="重命名应用"
      :confirm-loading="renaming"
      @ok="doRename"
    >
      <a-form layout="vertical">
        <a-form-item label="应用名称" required>
          <a-input v-model:value="renameForm.appName" placeholder="请输入新的应用名称" allow-clear />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 应用详情 -->
    <!-- 注意 AppDetailModal 的 edit / delete 事件【不带任何参数】，
         所以不能直接 @delete="doDelete"，否则拿到的是 undefined，点了没反应 -->
    <AppDetailModal
      v-model:open="detailVisible"
      :app="detailApp"
      :show-actions="true"
      @edit="openRenameModal(detailApp)"
      @delete="deleteDetailApp"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  addApp,
  deleteApp,
  listMyAppVoByPage,
  updateApp,
} from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import { getStaticPreviewUrl } from '@/config/env'
import AppDetailModal from '@/components/AppDetailModal.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const columns = [
  // 不展示 id 列：一个是内部主键没必要暴露给用户，
  // 另一个是 18 位的 Long 在窄列里会折成好几行，很难看
  { title: '应用名称', dataIndex: 'appName', width: 180 },
  { title: '封面', dataIndex: 'cover', width: 100 },
  { title: '初始提示词', dataIndex: 'initPrompt', width: 220 },
  { title: '生成类型', dataIndex: 'codeGenType', width: 120 },
  { title: '部署状态', dataIndex: 'deployedTime', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMyAppVoByPage({ ...searchParams })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取我的应用失败：', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`,
}))

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const doResetSearch = () => {
  searchParams.appName = undefined
  searchParams.codeGenType = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 后端 Long 序列化成字符串，接口类型声明是 number，运行时传字符串也能被正确解析
const toId = (id?: number | string) => id as unknown as number

// ===== 新建 =====
const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive<API.AppAddRequest>({ initPrompt: '' })

const openCreateModal = () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  createForm.initPrompt = ''
  createVisible.value = true
}

const doCreate = async () => {
  if (!createForm.initPrompt?.trim()) {
    message.warning('请先描述你想要的网站')
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt: createForm.initPrompt })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功，正在进入对话页')
      createVisible.value = false
      // 创建成功后直接进对话页，前端会自动用初始提示词发起生成
      await router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// ===== 重命名 =====
const renameVisible = ref(false)
const renaming = ref(false)
const renameForm = reactive<{ id?: number; appName?: string }>({ id: undefined, appName: '' })

const openRenameModal = (record?: API.AppVO) => {
  if (!record?.id) return
  renameForm.id = toId(record.id)
  renameForm.appName = record.appName ?? ''
  renameVisible.value = true
}

const doRename = async () => {
  if (!renameForm.id || !renameForm.appName?.trim()) {
    message.warning('应用名称不能为空')
    return
  }
  renaming.value = true
  try {
    // 后端 POST /app/update 只允许改应用名，其它字段改了也不会生效
    const res = await updateApp({ id: renameForm.id, appName: renameForm.appName.trim() })
    if (res.data.code === 0) {
      message.success('重命名成功')
      renameVisible.value = false
      fetchData()
    } else {
      message.error('重命名失败：' + res.data.message)
    }
  } catch (error) {
    console.error('重命名失败：', error)
    message.error('重命名失败，请重试')
  } finally {
    renaming.value = false
  }
}

// ===== 删除 =====
const doDelete = async (id?: number | string) => {
  if (!id) return
  try {
    const res = await deleteApp({ id: toId(id) })
    if (res.data.code === 0) {
      message.success('删除成功')
      detailVisible.value = false
      // 删掉当前页最后一条时往前翻一页，避免停在空页
      if (data.value.length === 1 && (searchParams.pageNum ?? 1) > 1) {
        searchParams.pageNum = (searchParams.pageNum ?? 1) - 1
      }
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败，请重试')
  }
}

// ===== 详情 / 预览 / 对话 =====
const detailVisible = ref(false)
// AppDetailModal 的 app prop 类型是 AppVO | undefined，不接受 null
const detailApp = ref<API.AppVO | undefined>(undefined)

const openDetail = (record: API.AppVO) => {
  detailApp.value = record
  detailVisible.value = true
}

// 详情弹窗里的「删除」事件不带参数，这里从当前详情对象取 id
const deleteDetailApp = () => {
  if (detailApp.value?.id) {
    doDelete(detailApp.value.id)
  }
}

const openPreview = (record: API.AppVO) => {
  if (!record.id) return
  if (!record.codeGenType) {
    message.warning('该应用缺少生成类型，暂时无法预览')
    return
  }
  const url = getStaticPreviewUrl(String(record.codeGenType), String(record.id))
  window.open(url, '_blank')
}

const goChat = (record: API.AppVO) => {
  if (!record.id) return
  router.push(`/app/chat/${record.id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#myAppsPage {
  max-width: 1280px;
  margin: 16px auto;
}

.main-card {
  border-radius: 12px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page-desc {
  margin-top: 6px;
  color: #8c8c8c;
  font-size: 13px;
}

.search-form {
  margin-bottom: 16px;
}

.no-cover {
  width: 80px;
  height: 60px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  border-radius: 4px;
}

.prompt-text {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-gray {
  color: #999;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
