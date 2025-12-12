<template>
  <div class="admin-submissions-page">
    <div class="container">
      <div class="page-header">
        <h1>工具提交审核</h1>
        <div class="header-stats">
          <span class="stat-item">
            待审核: <strong>{{ pendingCount }}</strong>
          </span>
        </div>
      </div>

      <!-- Filters -->
      <div class="filters">
        <button
          :class="['filter-btn', { active: statusFilter === null }]"
          @click="statusFilter = null; loadSubmissions()"
        >
          全部
        </button>
        <button
          :class="['filter-btn', { active: statusFilter === 0 }]"
          @click="statusFilter = 0; loadSubmissions()"
        >
          待审核
        </button>
        <button
          :class="['filter-btn', { active: statusFilter === 1 }]"
          @click="statusFilter = 1; loadSubmissions()"
        >
          已通过
        </button>
        <button
          :class="['filter-btn', { active: statusFilter === 2 }]"
          @click="statusFilter = 2; loadSubmissions()"
        >
          已拒绝
        </button>
      </div>

      <!-- Submissions List -->
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="submissions.length === 0" class="empty">暂无提交记录</div>
      <div v-else class="submissions-list">
        <div
          v-for="submission in submissions"
          :key="submission.id"
          class="submission-card"
        >
          <!-- Header -->
          <div class="card-header">
            <div class="tool-info">
              <img
                v-if="submission.logoUrl"
                :src="submission.logoUrl"
                alt="logo"
                class="tool-logo"
              />
              <div>
                <h3>{{ submission.name }}</h3>
                <p class="tagline">{{ submission.tagline }}</p>
              </div>
            </div>
            <span :class="`status-badge status-${submission.status}`">
              {{ submission.statusDesc }}
            </span>
          </div>

          <!-- Content -->
          <div class="card-content">
            <div class="info-row">
              <span class="label">描述:</span>
              <p>{{ submission.description }}</p>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">分类:</span>
                <span>{{ submission.categoryName }}</span>
              </div>
              <div class="info-item">
                <span class="label">定价:</span>
                <span>{{ formatPricingModel(submission.pricingModel) }}</span>
              </div>
              <div class="info-item">
                <span class="label">官网:</span>
                <a :href="submission.websiteUrl" target="_blank">
                  {{ submission.websiteUrl }}
                </a>
              </div>
              <div class="info-item">
                <span class="label">提交人:</span>
                <span>{{ submission.username }}</span>
              </div>
            </div>
            <div class="info-row">
              <span class="label">提交时间:</span>
              <span>{{ formatDateTime(submission.createdAt) }}</span>
            </div>

            <!-- Review Info (if reviewed) -->
            <div v-if="submission.status !== 0" class="review-info">
              <div class="info-row">
                <span class="label">审核人:</span>
                <span>{{ submission.reviewerName || '未知' }}</span>
              </div>
              <div class="info-row">
                <span class="label">审核时间:</span>
                <span>{{ formatDateTime(submission.reviewedAt) }}</span>
              </div>
              <div v-if="submission.reviewComment" class="info-row">
                <span class="label">审核意见:</span>
                <p>{{ submission.reviewComment }}</p>
              </div>
            </div>
          </div>

          <!-- Actions (only for pending) -->
          <div v-if="submission.status === 0" class="card-actions">
            <button class="btn-reject" @click="openReviewDialog(submission, 2)">
              拒绝
            </button>
            <button class="btn-approve" @click="openReviewDialog(submission, 1)">
              通过
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- Review Dialog -->
    <div v-if="showReviewDialog" class="modal-overlay" @click="closeReviewDialog">
      <div class="modal-content" @click.stop>
        <h2>{{ reviewAction === 1 ? '通过审核' : '拒绝提交' }}</h2>
        <p class="modal-desc">
          {{ reviewAction === 1 ? '确认通过这个工具的提交吗？通过后将自动发布到工具列表。' : '请填写拒绝原因:' }}
        </p>

        <textarea
          v-if="reviewAction === 2"
          v-model="reviewComment"
          rows="4"
          placeholder="请说明拒绝的原因..."
          class="review-comment-input"
          required
        ></textarea>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeReviewDialog">取消</button>
          <button
            class="btn-confirm"
            @click="submitReview"
            :disabled="reviewAction === 2 && !reviewComment.trim()"
          >
            确认
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllSubmissions, reviewSubmission, getPendingCount } from '../api/submission'

const submissions = ref([])
const loading = ref(false)
const statusFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)
const pendingCount = ref(0)

const showReviewDialog = ref(false)
const currentSubmission = ref(null)
const reviewAction = ref(1) // 1-通过 2-拒绝
const reviewComment = ref('')

// 加载提交列表
const loadSubmissions = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }

    const res = await getAllSubmissions(params)
    if (res.data) {
      submissions.value = res.data.records || []
      totalPages.value = res.data.pages || 0
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载待审核数量
const loadPendingCount = async () => {
  try {
    const res = await getPendingCount()
    if (res.data !== undefined) {
      pendingCount.value = res.data
    }
  } catch (error) {
    console.error('加载待审核数量失败:', error)
  }
}

// 打开审核对话框
const openReviewDialog = (submission, action) => {
  currentSubmission.value = submission
  reviewAction.value = action
  reviewComment.value = ''
  showReviewDialog.value = true
}

// 关闭审核对话框
const closeReviewDialog = () => {
  showReviewDialog.value = false
  currentSubmission.value = null
  reviewComment.value = ''
}

// 提交审核
const submitReview = async () => {
  if (reviewAction.value === 2 && !reviewComment.value.trim()) {
    alert('请填写拒绝原因')
    return
  }

  try {
    await reviewSubmission(currentSubmission.value.id, {
      status: reviewAction.value,
      reviewComment: reviewComment.value || null
    })

    alert(reviewAction.value === 1 ? '审核通过' : '已拒绝')
    closeReviewDialog()
    await loadSubmissions()
    await loadPendingCount()
  } catch (error) {
    console.error('审核失败:', error)
    alert(error.response?.data?.message || '操作失败，请稍后重试')
  }
}

// 切换页码
const changePage = (page) => {
  currentPage.value = page
  loadSubmissions()
}

// 格式化定价模式
const formatPricingModel = (model) => {
  const map = {
    'FREE': '免费',
    'FREEMIUM': '免费增值',
    'PAID': '付费',
    'SUBSCRIPTION': '订阅'
  }
  return map[model] || model
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadSubmissions()
  loadPendingCount()
})
</script>

<style scoped>
.admin-submissions-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding: 40px 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
}

.header-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  font-size: 14px;
  color: #666;
}

.stat-item strong {
  font-size: 20px;
  color: #1890ff;
  margin-left: 8px;
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.filter-btn {
  padding: 8px 16px;
  font-size: 14px;
  border: 1px solid #e8e8e8;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.filter-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.loading, .empty {
  text-align: center;
  padding: 60px 20px;
  font-size: 16px;
  color: #999;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.submission-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.tool-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-logo {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.tool-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.tagline {
  font-size: 14px;
  color: #666;
}

.status-badge {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
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

.card-content {
  padding: 20px;
}

.info-row {
  margin-bottom: 12px;
  font-size: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.info-item {
  font-size: 14px;
}

.label {
  font-weight: 500;
  color: #666;
  margin-right: 8px;
}

.info-row p, .info-item span {
  color: #333;
}

.info-item a {
  color: #1890ff;
  text-decoration: none;
}

.info-item a:hover {
  text-decoration: underline;
}

.review-info {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.card-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.btn-reject, .btn-approve {
  padding: 8px 20px;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reject {
  background: white;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
}

.btn-reject:hover {
  background: #ff4d4f;
  color: white;
}

.btn-approve {
  background: #52c41a;
  border: none;
  color: white;
}

.btn-approve:hover {
  background: #73d13d;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
}

.pagination button {
  padding: 8px 16px;
  font-size: 14px;
  border: 1px solid #e8e8e8;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 32px;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
}

.modal-content h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.modal-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.review-comment-input {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 20px;
}

.review-comment-input:focus {
  outline: none;
  border-color: #1890ff;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel, .btn-confirm {
  padding: 8px 20px;
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

.btn-confirm {
  background: #1890ff;
  border: none;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
