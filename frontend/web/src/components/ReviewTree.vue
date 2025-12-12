<template>
  <div class="review-tree">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="reviews.length === 0" class="empty">
      <p>暂无评论，快来发表第一条评论吧~</p>
    </div>

    <div v-else class="reviews-list">
      <ReviewItem
        v-for="review in reviews"
        :key="review.id"
        :review="review"
        @reply="handleReply"
        @delete="handleDelete"
        @helpful="handleHelpful"
      />
    </div>

    <!-- 回复对话框 -->
    <div v-if="replyDialogVisible" class="dialog-overlay" @click="closeDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>回复评论</h3>
          <button class="close-btn" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <textarea
            v-model="replyContent"
            placeholder="请输入回复内容..."
            rows="4"
            maxlength="500"
            class="reply-textarea"
          ></textarea>
          <div class="char-count">{{ replyContent.length }}/500</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="closeDialog">取消</button>
          <button class="btn btn-primary" @click="submitReply" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交回复' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReviewsTree, createReview, deleteReview, markHelpful, unmarkHelpful } from '../api/review'
import ReviewItem from './ReviewItem.vue'
import toast from '@/utils/toast'

const props = defineProps({
  toolId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['review-created', 'review-deleted'])

const loading = ref(false)
const reviews = ref([])
const replyDialogVisible = ref(false)
const replyContent = ref('')
const replyToReview = ref(null)
const submitting = ref(false)

// 加载评论树
const loadReviews = async () => {
  try {
    loading.value = true
    const res = await getReviewsTree(props.toolId)
    if (res.code === 200) {
      reviews.value = res.data || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    toast.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

// 处理回复
const handleReply = (review) => {
  replyToReview.value = review
  replyContent.value = ''
  replyDialogVisible.value = true
}

// 关闭对话框
const closeDialog = () => {
  replyDialogVisible.value = false
  replyContent.value = ''
  replyToReview.value = null
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    toast.warning('请输入回复内容')
    return
  }

  try {
    submitting.value = true
    const res = await createReview({
      toolId: props.toolId,
      parentId: replyToReview.value.id,
      content: replyContent.value.trim()
    })

    if (res.code === 200) {
      toast.success('回复成功')
      closeDialog()
      // 重新加载评论树
      await loadReviews()
      emit('review-created')
    }
  } catch (error) {
    console.error('回复失败:', error)
    toast.error(error.response?.data?.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

// 处理删除
const handleDelete = async (review) => {
  if (!confirm('确定要删除这条评论吗？')) {
    return
  }

  try {
    const res = await deleteReview(review.id)
    if (res.code === 200) {
      toast.success('删除成功')
      await loadReviews()
      emit('review-deleted')
    }
  } catch (error) {
    console.error('删除失败:', error)
    toast.error(error.response?.data?.message || '删除失败')
  }
}

// 处理有帮助标记
const handleHelpful = async (review) => {
  try {
    if (review.isHelpful) {
      await unmarkHelpful(review.id)
      toast.success('已取消标记')
    } else {
      await markHelpful(review.id)
      toast.success('标记成功')
    }
    await loadReviews()
  } catch (error) {
    console.error('操作失败:', error)
    toast.error(error.response?.data?.message || '操作失败')
  }
}

// 对外暴露刷新方法
defineExpose({
  loadReviews
})

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.review-tree {
  width: 100%;
}

.loading,
.empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 对话框样式 */
.dialog-overlay {
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
  animation: fadeIn 0.2s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.dialog-body {
  padding: 20px;
}

.reply-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s;
}

.reply-textarea:focus {
  outline: none;
  border-color: #1890ff;
}

.char-count {
  text-align: right;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.dialog-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 1px solid #e8e8e8;
}

.btn-cancel:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
}
</style>
