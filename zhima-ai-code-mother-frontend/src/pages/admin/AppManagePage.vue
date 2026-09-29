<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="创建者">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchParams.codeGenType"
          placeholder="选择生成类型"
          style="width: 150px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option
            v-for="option in CODE_GEN_TYPE_OPTIONS"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="精选状态">
        <!-- 后端「精选」的判定是 priority 严格等于 99（见 /app/good/list/page/vo），
             所以这里只给「精选 / 未精选」两个值，清空表示不筛选 -->
        <a-select
          v-model:value="searchParams.priority"
          placeholder="全部"
          style="width: 130px"
          allow-clear
        >
          <a-select-option :value="99">只看精选</a-select-option>
          <a-select-option :value="0">只看未精选</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="doResetSearch">重置</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1200 }"
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
          {{ formatCodeGenType(record.codeGenType) }}
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.priority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">
            {{ formatTime(record.deployedTime) }}
          </span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <UserInfo :user="record.user" size="small" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="openDetail(record.id)"> 详情 </a-button>
            <a-button type="primary" size="small" @click="editApp(record)"> 编辑 </a-button>
            <a-button size="small" @click="openFeatureModal(record)"> 编辑精选 </a-button>
            <a-button
              type="default"
              size="small"
              @click="toggleFeatured(record)"
              :class="{ 'featured-btn': record.priority === 99 }"
            >
              {{ record.priority === 99 ? '取消精选' : '精选' }}
            </a-button>
            <a-popconfirm title="确定要删除这个应用吗？" @confirm="deleteApp(record.id)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 应用详情（管理员） -->
    <a-modal v-model:open="detailVisible" title="应用详情" :footer="null" width="640px">
      <a-descriptions v-if="detail" :column="1" bordered size="small">
        <a-descriptions-item label="ID">{{ detail.id }}</a-descriptions-item>
        <a-descriptions-item label="应用名称">{{ detail.appName }}</a-descriptions-item>
        <a-descriptions-item label="生成类型">
          {{ formatCodeGenType(detail.codeGenType) }}
        </a-descriptions-item>
        <a-descriptions-item label="初始提示词">
          {{ detail.initPrompt || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="优先级">
          <a-tag v-if="detail.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ detail.priority || 0 }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="部署密钥">
          {{ detail.deployKey || '未部署' }}
        </a-descriptions-item>
        <a-descriptions-item label="部署时间">
          {{ detail.deployedTime ? formatTime(detail.deployedTime) : '未部署' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建者">
          <UserInfo v-if="detail.user" :user="detail.user" size="small" />
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="封面">
          <a-image v-if="detail.cover" :src="detail.cover" :width="160" />
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatTime(detail.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ formatTime(detail.updateTime) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 编辑精选案例 -->
    <a-modal
      v-model:open="featureVisible"
      title="编辑精选案例"
      :confirm-loading="savingFeature"
      ok-text="保存"
      @ok="doSaveFeature"
    >
      <a-form :model="featureForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="应用名称" required>
          <a-input v-model:value="featureForm.appName" placeholder="展示在首页卡片上的名称" />
        </a-form-item>
        <a-form-item label="封面地址">
          <a-input
            v-model:value="featureForm.cover"
            placeholder="图片 URL，留空则不显示封面"
            allow-clear
          />
          <div class="form-tip">首页「精选案例」卡片上的封面图</div>
        </a-form-item>
        <a-form-item label="封面预览">
          <a-image v-if="featureForm.cover" :src="featureForm.cover" :width="180" />
          <span v-else class="text-gray">未设置</span>
        </a-form-item>
        <a-form-item label="设为精选">
          <a-switch v-model:checked="featureForm.featured" />
          <div class="form-tip">
            开启后该应用会出现在首页「精选案例」（priority=99），关闭则移出（priority=0）
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
  getAppVoByIdByAdmin,
} from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    fixed: 'left',
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: '部署时间',
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: '创建者',
    dataIndex: 'user',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAppVoByPageByAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索条件
const doResetSearch = () => {
  searchParams.appName = undefined
  searchParams.userId = undefined
  searchParams.codeGenType = undefined
  searchParams.priority = undefined
  searchParams.pageNum = 1
  fetchData()
}

// ===== 编辑精选案例 =====
const featureVisible = ref(false)
const savingFeature = ref(false)
const featureForm = reactive<{
  id?: number
  appName?: string
  cover?: string
  featured: boolean
}>({
  id: undefined,
  appName: '',
  cover: '',
  featured: false,
})

const openFeatureModal = (record: API.AppVO) => {
  if (!record.id) return
  featureForm.id = record.id
  featureForm.appName = record.appName ?? ''
  featureForm.cover = record.cover ?? ''
  // 后端判定「精选」用的是 priority 严格等于 99（见 /app/good/list/page/vo），
  // 所以这里映射成一个开关，而不是让人填 0~99 的数字 —— 填 50 是不会出现在首页的。
  featureForm.featured = record.priority === 99
  featureVisible.value = true
}

const doSaveFeature = async () => {
  if (!featureForm.id) return
  if (!featureForm.appName?.trim()) {
    message.warning('应用名称不能为空')
    return
  }
  savingFeature.value = true
  try {
    const res = await updateAppByAdmin({
      id: featureForm.id,
      appName: featureForm.appName.trim(),
      // 传空字符串是「清空封面」，传 undefined 才会被后端忽略
      cover: featureForm.cover ?? '',
      priority: featureForm.featured ? 99 : 0,
    })
    if (res.data.code === 0) {
      message.success(featureForm.featured ? '已保存，并设为精选案例' : '已保存，并移出精选案例')
      featureVisible.value = false
      fetchData()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    console.error('保存精选信息失败：', error)
    message.error('保存失败，请重试')
  } finally {
    savingFeature.value = false
  }
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 应用详情
const detailVisible = ref(false)
const detail = ref<API.AppVO | null>(null)

const openDetail = async (id?: number) => {
  if (!id) return
  try {
    const res = await getAppVoByIdByAdmin({ id })
    if (res.data.code === 0 && res.data.data) {
      detail.value = res.data.data
      detailVisible.value = true
    } else {
      message.error('获取详情失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取应用详情失败：', error)
    message.error('获取详情失败')
  }
}

// 删除应用
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#appManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
  /* 直角改圆角；overflow:hidden 用来裁掉表格表头方角 */
  border-radius: 16px;
  overflow: hidden;
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
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-gray {
  color: #999;
}

.featured-btn {
  background: #faad14;
  border-color: #faad14;
  color: white;
}

.featured-btn:hover {
  background: #d48806;
  border-color: #d48806;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
