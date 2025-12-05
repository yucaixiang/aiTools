<template>
  <div class="categories-list">
    <el-card>
      <template #header>
        <div class="header">
          <span>分类管理</span>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <el-table :data="categoriesList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="toolsCount" label="工具数量" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.toolsCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="9999"
            placeholder="排序值越小越靠前"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoriesList,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/admin'

const loading = ref(false)
const categoriesList = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('add') // 'add' 或 'edit'
const submitting = ref(false)

const formRef = ref(null)
const form = ref({
  id: null,
  name: '',
  description: '',
  sort: 0
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  return dialogMode.value === 'add' ? '新增分类' : '编辑分类'
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
  loading.value = true
  try {
    const res = await getCategoriesList()
    categoriesList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  form.value = {
    id: row.id,
    name: row.name,
    description: row.description || '',
    sort: row.sort || 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      name: form.value.name,
      description: form.value.description,
      sort: form.value.sort
    }

    if (dialogMode.value === 'add') {
      await createCategory(data)
      ElMessage.success('新增成功')
    } else {
      await updateCategory(form.value.id, data)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  if (row.toolsCount > 0) {
    ElMessage.warning('该分类下还有工具，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    name: '',
    description: '',
    sort: 0
  }
  formRef.value?.resetFields()
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.categories-list {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
