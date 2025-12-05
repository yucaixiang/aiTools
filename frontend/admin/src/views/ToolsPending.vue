<template>
  <div class="tools-pending">
    <el-card>
      <template #header>
        <div class="header">
          <span>待审核工具</span>
          <el-badge :value="total" class="badge">
            <el-button size="small" @click="loadTools">刷新</el-button>
          </el-badge>
        </div>
      </template>

      <el-table :data="toolsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="工具名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="url" label="访问地址" width="200">
          <template #default="{ row }">
            <el-link :href="row.url" target="_blank" type="primary">{{ row.url }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="submitterName" label="提交者" width="120" />
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看详情</el-button>
            <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="warning" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="mt-20"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工具详情" width="800px">
      <el-descriptions :column="2" border v-if="currentTool">
        <el-descriptions-item label="工具名称">{{ currentTool.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentTool.category }}</el-descriptions-item>
        <el-descriptions-item label="访问地址" :span="2">
          <el-link :href="currentTool.url" target="_blank" type="primary">
            {{ currentTool.url }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentTool.description }}
        </el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">
          <el-tag v-for="tag in currentTool.tags" :key="tag" class="mr-10">{{ tag }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交者">{{ currentTool.submitterName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDate(currentTool.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="handleApproveFromDetail">通过</el-button>
        <el-button type="warning" @click="handleRejectFromDetail">拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝工具" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="工具名称">
          <span>{{ rejectForm.toolName }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，该原因将通知给提交者"
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
import { getToolsList, approveTool, rejectTool } from '@/api/admin'

const loading = ref(false)
const toolsList = ref([])
const total = ref(0)

const queryParams = ref({
  page: 1,
  pageSize: 10,
  status: 'pending'
})

const detailDialogVisible = ref(false)
const currentTool = ref(null)

const rejectDialogVisible = ref(false)
const rejectForm = ref({
  id: null,
  toolName: '',
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

const loadTools = async () => {
  loading.value = true
  try {
    const res = await getToolsList(queryParams.value)
    toolsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载待审核工具列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadTools()
}

const handleView = (row) => {
  currentTool.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过工具"${row.name}"吗？通过后该工具将在前台展示。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    await approveTool(row.id)
    ElMessage.success('已通过审核')
    loadTools()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleApproveFromDetail = async () => {
  if (!currentTool.value) return
  detailDialogVisible.value = false
  await handleApprove(currentTool.value)
}

const handleReject = (row) => {
  rejectForm.value = {
    id: row.id,
    toolName: row.name,
    reason: ''
  }
  rejectDialogVisible.value = true
}

const handleRejectFromDetail = () => {
  if (!currentTool.value) return
  detailDialogVisible.value = false
  handleReject(currentTool.value)
}

const confirmReject = async () => {
  if (!rejectForm.value.reason || rejectForm.value.reason.trim() === '') {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    await rejectTool(rejectForm.value.id, rejectForm.value.reason)
    ElMessage.success('已拒绝该工具')
    rejectDialogVisible.value = false
    loadTools()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadTools()
})
</script>

<style scoped lang="scss">
.tools-pending {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;

    .badge {
      margin-left: 10px;
    }
  }

  .mr-10 {
    margin-right: 10px;
  }
}
</style>
