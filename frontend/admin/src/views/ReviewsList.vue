<template>
  <div class="reviews-list">
    <el-card>
      <template #header>
        <div class="header">
          <span>评论管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="工具名称">
            <el-input
              v-model="queryParams.toolName"
              placeholder="请输入工具名称"
              clearable
              @clear="handleQuery"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="queryParams.username"
              placeholder="请输入用户名"
              clearable
              @clear="handleQuery"
            />
          </el-form-item>
          <el-form-item label="评分">
            <el-select
              v-model="queryParams.rating"
              placeholder="请选择评分"
              clearable
              @clear="handleQuery"
            >
              <el-option label="5星" :value="5" />
              <el-option label="4星" :value="4" />
              <el-option label="3星" :value="3" />
              <el-option label="2星" :value="2" />
              <el-option label="1星" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="reviewsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="toolName" label="工具名称" width="200" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="rating" label="评分" width="150">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="评论时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 评论详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评论详情" width="600px">
      <el-descriptions :column="1" border v-if="currentReview">
        <el-descriptions-item label="评论ID">{{ currentReview.id }}</el-descriptions-item>
        <el-descriptions-item label="工具名称">{{ currentReview.toolName }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentReview.username }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentReview.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评论内容">
          <div style="white-space: pre-wrap;">{{ currentReview.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="评论时间">
          {{ formatDate(currentReview.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleDeleteFromDetail">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewsList, deleteReview } from '@/api/admin'

const loading = ref(false)
const reviewsList = ref([])
const total = ref(0)

const queryParams = ref({
  page: 1,
  pageSize: 10,
  toolName: '',
  username: '',
  rating: null
})

const detailDialogVisible = ref(false)
const currentReview = ref(null)

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

const loadReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.value.page,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.toolName) params.toolName = queryParams.value.toolName
    if (queryParams.value.username) params.username = queryParams.value.username
    if (queryParams.value.rating) params.rating = queryParams.value.rating

    const res = await getReviewsList(params)
    reviewsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.page = 1
  loadReviews()
}

const handleReset = () => {
  queryParams.value = {
    page: 1,
    pageSize: 10,
    toolName: '',
    username: '',
    rating: null
  }
  loadReviews()
}

const handleView = (row) => {
  currentReview.value = row
  detailDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.username}"的这条评论吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await deleteReview(row.id)
    ElMessage.success('删除成功')
    loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteFromDetail = async () => {
  if (!currentReview.value) return
  detailDialogVisible.value = false
  await handleDelete(currentReview.value)
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped lang="scss">
.reviews-list {
  .header {
    font-size: 16px;
    font-weight: 500;
  }

  .filter-bar {
    margin-bottom: 20px;
  }
}
</style>
