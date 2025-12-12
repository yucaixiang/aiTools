<template>
  <div class="review-item" :class="{ 'is-reply': !!review.parentId }" :style="getIndentStyle()">
    <!-- 评论头部 -->
    <div class="review-header">
      <div class="user-info">
        <img
          :src="review.userAvatar || '/default-avatar.png'"
          alt="avatar"
          class="avatar"
        />
        <span class="username">{{ review.username || '匿名用户' }}</span>
        <span class="time">{{ formatTime(review.createdAt) }}</span>
      </div>

      <div class="actions">
        <button
          class="action-btn"
          @click="$emit('reply', review)"
        >
          回复
        </button>
        <button
          v-if="review.isMine"
          class="action-btn delete-btn"
          @click="$emit('delete', review)"
        >
          删除
        </button>
      </div>
    </div>

    <!-- 评论标题（顶级评论才有） -->
    <div v-if="review.title && !review.parentId" class="review-title">
      {{ review.title }}
    </div>

    <!-- 评论内容 -->
    <div class="review-content">
      {{ review.content }}
    </div>

    <!-- 优缺点（顶级评论才有） -->
    <div v-if="(review.pros || review.cons) && !review.parentId" class="pros-cons">
      <div v-if="review.pros" class="pros">
        <strong>优点：</strong>{{ review.pros }}
      </div>
      <div v-if="review.cons" class="cons">
        <strong>缺点：</strong>{{ review.cons }}
      </div>
    </div>

    <!-- 评论底部 -->
    <div class="review-footer">
      <div class="left">
        <span v-if="review.usageDuration" class="usage">
          使用时长：{{ formatUsageDuration(review.usageDuration) }}
        </span>
      </div>

      <div class="right">
        <button
          class="helpful-btn"
          :class="{ active: review.isHelpful }"
          @click="$emit('helpful', review)"
        >
          <span class="icon">👍</span>
          有帮助 ({{ review.helpfulCount || 0 }})
        </button>

        <span v-if="review.replyCount > 0" class="reply-count">
          {{ review.replyCount }} 条回复
        </span>
      </div>
    </div>

    <!-- 回复列表 -->
    <div v-if="review.replies && review.replies.length > 0" class="replies-list">
      <ReviewItem
        v-for="reply in review.replies"
        :key="reply.id"
        :review="reply"
        :depth="depth + 1"
        @reply="$emit('reply', reply)"
        @delete="$emit('delete', reply)"
        @helpful="$emit('helpful', reply)"
      />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  review: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 0
  }
})

defineEmits(['reply', 'delete', 'helpful'])

// 计算缩进样式，最大缩进限制为3层
const getIndentStyle = () => {
  const maxDepth = 3
  const actualDepth = Math.min(props.depth, maxDepth)
  const indent = actualDepth * 40 // 每层缩进40px
  return {
    marginLeft: `${indent}px`
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 1分钟内
  if (diff < 60000) {
    return '刚刚'
  }
  // 1小时内
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 1天内
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 7天内
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  }
  // 超过7天显示日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化使用时长
const formatUsageDuration = (duration) => {
  const durationMap = {
    'WEEK': '一周',
    'MONTH': '一个月',
    'QUARTER': '三个月',
    'HALF_YEAR': '半年',
    'YEAR': '一年以上'
  }
  return durationMap[duration] || duration
}
</script>

<style scoped>
.review-item {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.review-item.is-reply {
  margin-top: 12px;
  background: #f7f8fa;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  font-size: 13px;
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 4px;
}

.action-btn:hover {
  background: #f0f8ff;
}

.delete-btn {
  color: #ff4d4f;
}

.delete-btn:hover {
  background: #fff1f0;
}

.review-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.pros-cons {
  margin: 12px 0;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 4px;
  font-size: 13px;
}

.pros,
.cons {
  margin: 6px 0;
  line-height: 1.6;
}

.pros strong {
  color: #52c41a;
}

.cons strong {
  color: #ff4d4f;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.usage {
  font-size: 12px;
  color: #999;
}

.right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.helpful-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 13px;
  background: none;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.helpful-btn .icon {
  font-size: 14px;
}

.helpful-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  background: #f0f8ff;
}

.helpful-btn.active {
  border-color: #1890ff;
  background: #1890ff;
  color: white;
}

.reply-count {
  font-size: 12px;
  color: #999;
}

.replies-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
