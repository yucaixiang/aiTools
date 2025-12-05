<template>
  <div class="tools-list">
    <el-card>
      <template #header>
        <div class="header">
          <span>工具管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="工具名称">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入工具名称"
              clearable
              @clear="handleQuery"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-select
              v-model="queryParams.categoryId"
              placeholder="请选择分类"
              clearable
              @clear="handleQuery"
            >
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择状态"
              clearable
              @clear="handleQuery"
            >
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="toolsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="工具名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="reviewCount" label="评论数" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="warning"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
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

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝工具" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToolsList, approveTool, rejectTool, deleteTool } from '@/api/admin'
import { getCategoriesList } from '@/api/admin'

const loading = ref(false)
const toolsList = ref([])
const total = ref(0)
const categories = ref([])

const queryParams = ref({
  page: 1,
  pageSize: 10,
  name: '',
  categoryId: '',
  status: ''
})

const rejectDialogVisible = ref(false)
const rejectForm = ref({
  id: null,
  reason: ''
})

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

const loadCategories = async () => {
  try {
    const res = await getCategoriesList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadTools = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.value.page,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.name) params.name = queryParams.value.name
    if (queryParams.value.categoryId) params.categoryId = queryParams.value.categoryId
    if (queryParams.value.status) params.status = queryParams.value.status

    const res = await getToolsList(params)
    toolsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载工具列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.page = 1
  loadTools()
}

const handleReset = () => {
  queryParams.value = {
    page: 1,
    pageSize: 10,
    name: '',
    categoryId: '',
    status: ''
  }
  loadTools()
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要通过工具"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await approveTool(row.id)
    ElMessage.success('操作成功')
    loadTools()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = (row) => {
  rejectForm.value = {
    id: row.id,
    reason: ''
  }
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    await rejectTool(rejectForm.value.id, rejectForm.value.reason)
    ElMessage.success('操作成功')
    rejectDialogVisible.value = false
    loadTools()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除工具"${row.name}"吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    await deleteTool(row.id)
    ElMessage.success('删除成功')
    loadTools()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadCategories()
  loadTools()
})
</script>

<style scoped lang="scss">
.tools-list {
  .header {
    font-size: 16px;
    font-weight: 500;
  }

  .filter-bar {
    margin-bottom: 20px;
  }
}
</style>
