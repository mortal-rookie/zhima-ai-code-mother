<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 工具栏 -->
    <div class="toolbar">
      <a-button type="primary" @click="openAddModal">新增用户</a-button>
    </div>
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      row-key="id"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image v-if="record.userAvatar" :src="record.userAvatar" :width="120" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDetail(record.id)">详情</a-button>
            <a-button type="link" @click="openEditModal(record)">编辑</a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增 / 编辑 用户 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      :confirm-loading="submitting"
      @ok="doSubmit"
    >
      <a-form :model="formData" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="账号" required>
          <!-- 后端 UserUpdateRequest 里没有 userAccount 字段，账号一旦创建就不能改 -->
          <a-input
            v-model:value="formData.userAccount"
            :disabled="isEdit"
            placeholder="登录账号，创建后不可修改"
          />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="formData.userName" placeholder="用户昵称" />
        </a-form-item>
        <a-form-item label="头像">
          <a-input v-model:value="formData.userAvatar" placeholder="图片 URL" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="formData.userProfile" :rows="3" placeholder="一句话介绍" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="formData.userRole">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <a-alert
        v-if="!isEdit"
        type="info"
        show-icon
        message="新用户的初始密码由后端固定为 12345678"
      />
    </a-modal>

    <!-- 用户详情 -->
    <a-modal v-model:open="detailVisible" title="用户详情" :footer="null">
      <a-descriptions v-if="detail" :column="1" bordered size="small">
        <a-descriptions-item label="id">{{ detail.id }}</a-descriptions-item>
        <a-descriptions-item label="账号">{{ detail.userAccount }}</a-descriptions-item>
        <a-descriptions-item label="昵称">{{ detail.userName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="角色">
          <a-tag :color="detail.userRole === 'admin' ? 'green' : 'blue'">
            {{ detail.userRole === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="简介">{{ detail.userProfile || '-' }}</a-descriptions-item>
        <a-descriptions-item label="头像">
          <a-image v-if="detail.userAvatar" :src="detail.userAvatar" :width="100" />
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ detail.createTime ? dayjs(detail.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  addUser,
  deleteUser,
  getUserVoById,
  listUserVoByPage,
  updateUser,
} from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

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

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 后端的 Long 会被序列化成字符串，但接口类型声明是 number，
// 运行时传字符串 Spring 也能正确转换成 Long，这里统一做一次收窄。
const toId = (id?: number | string) => id as unknown as number

// 删除数据
const doDelete = async (id?: number | string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id: toId(id) })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败：' + res.data.message)
  }
}

// ===== 新增 / 编辑 =====
const formVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const formData = reactive<{
  id?: number
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
}>({
  userRole: 'user',
})

const resetForm = () => {
  formData.id = undefined
  formData.userAccount = ''
  formData.userName = ''
  formData.userAvatar = ''
  formData.userProfile = ''
  formData.userRole = 'user'
}

const openAddModal = () => {
  isEdit.value = false
  resetForm()
  formVisible.value = true
}

const openEditModal = (record: API.UserVO) => {
  isEdit.value = true
  formData.id = toId(record.id)
  formData.userAccount = record.userAccount
  formData.userName = record.userName
  formData.userAvatar = record.userAvatar
  formData.userProfile = record.userProfile
  formData.userRole = record.userRole ?? 'user'
  formVisible.value = true
}

const doSubmit = async () => {
  if (!isEdit.value && !formData.userAccount) {
    message.error('请填写账号')
    return
  }
  submitting.value = true
  try {
    const res = isEdit.value
      ? await updateUser({
          id: toId(formData.id),
          userName: formData.userName,
          userAvatar: formData.userAvatar,
          userProfile: formData.userProfile,
          userRole: formData.userRole,
        })
      : await addUser({
          userAccount: formData.userAccount,
          userName: formData.userName,
          userAvatar: formData.userAvatar,
          userProfile: formData.userProfile,
          userRole: formData.userRole,
        })
    if (res.data.code === 0) {
      message.success(isEdit.value ? '修改成功' : '新增成功，初始密码 12345678')
      formVisible.value = false
      fetchData()
    } else {
      message.error((isEdit.value ? '修改失败：' : '新增失败：') + res.data.message)
    }
  } catch (error) {
    console.error('提交用户信息失败：', error)
    message.error('请求失败，请重试')
  } finally {
    submitting.value = false
  }
}

// ===== 详情 =====
const detailVisible = ref(false)
const detail = ref<API.UserVO | null>(null)

const openDetail = async (id?: number | string) => {
  if (!id) {
    return
  }
  try {
    const res = await getUserVoById({ id: toId(id) })
    if (res.data.code === 0 && res.data.data) {
      detail.value = res.data.data
      detailVisible.value = true
    } else {
      message.error('获取详情失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取用户详情失败：', error)
    message.error('获取详情失败，请重试')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
  /* 直角改圆角；overflow:hidden 用来裁掉表格表头方角，不然会在圆角外露出直角 */
  border-radius: 16px;
  overflow: hidden;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
