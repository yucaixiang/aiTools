<template>
  <div class="users-list">
    <el-card>
      <template #header>
        <div class="header">
          <span>用户管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="用户名">
            <el-input
              v-model="queryParams.username"
              placeholder="请输入用户名"
              clearable
              @clear="handleQuery"
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input
              v-model="queryParams.email"
              placeholder="请输入邮箱"
              clearable
              @clear="handleQuery"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select
              v-model="queryParams.role"
              placeholder="请选择角色"
              clearable
              @clear="handleQuery"
            >
              <el-option label="普通用户" value="user" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="usersList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="toolsCount" label="提交工具数" width="120" />
        <el-table-column prop="reviewsCount" label="评论数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'active'" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'admin'"
              :type="row.status === 'active' ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="mt-20"
      />
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="800px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-if="currentUser.role === 'admin'" type="danger">管理员</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentUser.status === 'active'" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交工具数">
          {{ currentUser.toolsCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="评论数">
          {{ currentUser.reviewsCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="收藏数">
          {{ currentUser.favoritesCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">
          {{ formatDate(currentUser.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录" :span="2">
          {{ formatDate(currentUser.lastLoginAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersList } from '@/api/admin'

const loading = ref(false)
const usersList = ref([])
const total = ref(0)

const queryParams = ref({
  page: 1,
  pageSize: 10,
  username: '',
  email: '',
  role: ''
})

const detailDialogVisible = ref(false)
const currentUser = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.value.page,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.username) params.username = queryParams.value.username
    if (queryParams.value.email) params.email = queryParams.value.email
    if (queryParams.value.role) params.role = queryParams.value.role

    const res = await getUsersList(params)
    usersList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.page = 1
  loadUsers()
}

const handleReset = () => {
  queryParams.value = {
    page: 1,
    pageSize: 10,
    username: '',
    email: '',
    role: ''
  }
  loadUsers()
}

const handleToggleStatus = async (row) => {
  const action = row.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${row.username}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API更新用户状态
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleView = (row) => {
  currentUser.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.users-list {
  .header {
    font-size: 16px;
    font-weight: 500;
  }

  .filter-bar {
    margin-bottom: 20px;
  }
}
</style>
