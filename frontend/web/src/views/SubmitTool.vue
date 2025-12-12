<template>
  <div class="submit-tool-page">
    <div class="container">
      <h1 class="page-title">提交新工具</h1>
      <p class="page-desc">分享你发现的优质工具，帮助更多人提升效率</p>

      <form class="submit-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="required">工具名称</label>
          <input
            v-model="formData.name"
            type="text"
            placeholder="请输入工具名称"
            required
          />
        </div>

        <div class="form-group">
          <label class="required">工具标语</label>
          <input
            v-model="formData.tagline"
            type="text"
            placeholder="用一句话介绍这个工具"
            required
          />
        </div>

        <div class="form-group">
          <label class="required">工具描述</label>
          <textarea
            v-model="formData.description"
            rows="6"
            placeholder="详细描述这个工具的功能和特点"
            required
          ></textarea>
        </div>

        <div class="form-group">
          <label class="required">官网地址</label>
          <input
            v-model="formData.websiteUrl"
            type="url"
            placeholder="https://example.com"
            required
          />
        </div>

        <div class="form-group">
          <label>Logo URL</label>
          <input
            v-model="formData.logoUrl"
            type="url"
            placeholder="https://example.com/logo.png"
          />
          <div v-if="formData.logoUrl" class="logo-preview">
            <img :src="formData.logoUrl" alt="Logo Preview" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="required">分类</label>
            <select v-model="formData.categoryId" required>
              <option value="">请选择分类</option>
              <option
                v-for="category in categories"
                :key="category.id"
                :value="category.id"
              >
                {{ category.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label class="required">定价模式</label>
            <select v-model="formData.pricingModel" required>
              <option value="">请选择定价模式</option>
              <option value="FREE">免费</option>
              <option value="FREEMIUM">免费增值</option>
              <option value="PAID">付费</option>
              <option value="SUBSCRIPTION">订阅</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>发布日期</label>
          <input
            v-model="formData.launchDate"
            type="date"
          />
        </div>

        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="$router.back()">
            取消
          </button>
          <button type="submit" class="btn-submit" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交审核' }}
          </button>
        </div>
      </form>

      <!-- My Submissions Section -->
      <div class="my-submissions" v-if="mySubmissions.length > 0">
        <h2>我的提交</h2>
        <div class="submissions-list">
          <div
            v-for="submission in mySubmissions"
            :key="submission.id"
            class="submission-item"
          >
            <div class="submission-info">
              <h3>{{ submission.name }}</h3>
              <p>{{ submission.tagline }}</p>
              <div class="submission-meta">
                <span class="category">{{ submission.categoryName }}</span>
                <span class="date">{{ formatDate(submission.createdAt) }}</span>
              </div>
            </div>
            <div class="submission-status">
              <span :class="`status-badge status-${submission.status}`">
                {{ submission.statusDesc }}
              </span>
              <p v-if="submission.reviewComment" class="review-comment">
                {{ submission.reviewComment }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { submitTool, getMySubmissions } from '../api/submission'
import { getCategories } from '../api/tool'

const router = useRouter()

const formData = ref({
  name: '',
  tagline: '',
  description: '',
  websiteUrl: '',
  logoUrl: '',
  categoryId: '',
  pricingModel: '',
  launchDate: ''
})

const categories = ref([])
const mySubmissions = ref([])
const submitting = ref(false)

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.data && Array.isArray(res.data)) {
      // 只显示父分类
      categories.value = res.data.filter(cat => !cat.parentId)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载我的提交
const loadMySubmissions = async () => {
  try {
    const res = await getMySubmissions({ current: 1, size: 10 })
    if (res.data && res.data.records) {
      mySubmissions.value = res.data.records
    }
  } catch (error) {
    console.error('加载提交历史失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (submitting.value) return

  submitting.value = true
  try {
    const submitData = {
      ...formData.value,
      categoryId: parseInt(formData.value.categoryId)
    }

    // 如果有launchDate，转换为ISO格式
    if (submitData.launchDate) {
      submitData.launchDate = new Date(submitData.launchDate).toISOString()
    } else {
      delete submitData.launchDate
    }

    await submitTool(submitData)

    alert('提交成功！请等待管理员审核')

    // 重置表单
    formData.value = {
      name: '',
      tagline: '',
      description: '',
      websiteUrl: '',
      logoUrl: '',
      categoryId: '',
      pricingModel: '',
      launchDate: ''
    }

    // 重新加载提交列表
    await loadMySubmissions()
  } catch (error) {
    console.error('提交失败:', error)
    alert(error.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadCategories()
  loadMySubmissions()
})
</script>

<style scoped>
.submit-tool-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding: 40px 20px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
}

.page-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 40px;
}

.submit-form {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.form-group {
  margin-bottom: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

label.required::after {
  content: ' *';
  color: #ff4d4f;
}

input, textarea, select {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  transition: border-color 0.2s;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: #1890ff;
}

textarea {
  resize: vertical;
  font-family: inherit;
}

.logo-preview {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background: #fafafa;
}

.logo-preview img {
  max-width: 120px;
  max-height: 120px;
  object-fit: contain;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel, .btn-submit {
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  border: 1px solid #d9d9d9;
  color: #666;
}

.btn-cancel:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-submit {
  background: #1890ff;
  border: none;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.my-submissions {
  margin-top: 60px;
}

.my-submissions h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.submission-item {
  background: white;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.submission-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.submission-info p {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.submission-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.submission-meta .category {
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
}

.submission-status {
  text-align: right;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 4px;
  font-weight: 500;
}

.status-badge.status-0 {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.status-1 {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.status-2 {
  background: #fff1f0;
  color: #ff4d4f;
}

.review-comment {
  margin-top: 8px;
  font-size: 12px;
  color: #ff4d4f;
}
</style>
