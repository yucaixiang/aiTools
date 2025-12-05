<template>
  <view class="container">
    <!-- Header -->
    <view class="header">
      <view class="header-left">
        <text class="back-btn" @click="handleBack">‹</text>
        <text class="header-title">全部评论</text>
      </view>
      <text class="write-btn" @click="handleWriteReview">写评论</text>
    </view>

    <!-- Summary Section -->
    <view v-if="ratingDistribution" class="summary-section">
      <view class="average-rating">
        <text class="rating-number">{{ ratingDistribution.averageRating }}</text>
        <view class="stars">
          <text class="star" v-for="n in 5" :key="n">
            {{ n <= Math.round(ratingDistribution.averageRating) ? '⭐' : '☆' }}
          </text>
        </view>
        <text class="total-count">{{ ratingDistribution.totalCount }} 条评论</text>
      </view>

      <view class="rating-bars">
        <view
          class="rating-bar-item"
          v-for="item in ratingDistribution.distribution"
          :key="item.rating"
        >
          <text class="rating-label">{{ item.rating }}星</text>
          <view class="bar-container">
            <view
              class="bar-fill"
              :style="{ width: getBarWidth(item.count, ratingDistribution.totalCount) }"
            ></view>
          </view>
          <text class="rating-count">{{ item.count }}</text>
        </view>
      </view>
    </view>

    <!-- Sort Options -->
    <view class="sort-section">
      <view
        class="sort-item"
        :class="{ active: sortBy === 'latest' }"
        @click="handleSort('latest')"
      >
        <text>最新</text>
      </view>
      <view
        class="sort-item"
        :class="{ active: sortBy === 'helpful' }"
        @click="handleSort('helpful')"
      >
        <text>最有帮助</text>
      </view>
      <view
        class="sort-item"
        :class="{ active: sortBy === 'rating_high' }"
        @click="handleSort('rating_high')"
      >
        <text>评分最高</text>
      </view>
    </view>

    <!-- Reviews List -->
    <view class="reviews-list">
      <!-- Loading State -->
      <view v-if="loading && reviews.length === 0" class="loading">
        <text>加载中...</text>
      </view>

      <!-- Review Items -->
      <view
        v-for="review in reviews"
        :key="review.id"
        class="review-item"
      >
        <view class="review-header">
          <image class="user-avatar" :src="review.user?.avatar || defaultAvatar" />
          <view class="user-info">
            <text class="user-name">{{ review.user?.username || '匿名用户' }}</text>
            <view class="review-rating">
              <text class="star" v-for="n in 5" :key="n">
                {{ n <= review.rating ? '⭐' : '☆' }}
              </text>
            </view>
          </view>
          <text class="review-date">{{ formatDate(review.createdAt) }}</text>
        </view>

        <text class="review-content">{{ review.content }}</text>

        <view v-if="review.pros && review.pros.length > 0" class="review-pros-cons">
          <text class="pros-cons-title">👍 优点：</text>
          <text class="pros-cons-content">{{ review.pros.join('、') }}</text>
        </view>

        <view v-if="review.cons && review.cons.length > 0" class="review-pros-cons">
          <text class="pros-cons-title">👎 缺点：</text>
          <text class="pros-cons-content">{{ review.cons.join('、') }}</text>
        </view>

        <view class="review-actions">
          <view
            class="action-btn"
            :class="{ active: review.hasMarkedHelpful }"
            @click="handleMarkHelpful(review)"
          >
            <text class="action-icon">👍</text>
            <text class="action-text">
              {{ review.hasMarkedHelpful ? '已标记' : '有帮助' }}
            </text>
            <text class="action-count">({{ review.helpfulCount }})</text>
          </view>

          <view
            v-if="isMyReview(review)"
            class="action-btn"
            @click="handleEditReview(review)"
          >
            <text class="action-icon">✏️</text>
            <text class="action-text">编辑</text>
          </view>

          <view
            v-if="isMyReview(review)"
            class="action-btn delete"
            @click="handleDeleteReview(review)"
          >
            <text class="action-icon">🗑️</text>
            <text class="action-text">删除</text>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-if="!loading && reviews.length === 0" class="empty">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无评论</text>
        <button class="btn-write" @click="handleWriteReview">写第一条评论</button>
      </view>

      <!-- Load More -->
      <view v-if="!loading && hasMore" class="load-more">
        <button class="btn-load-more" @click="loadMore">加载更多</button>
      </view>

      <!-- No More -->
      <view v-if="!loading && reviews.length > 0 && !hasMore" class="no-more">
        <text>没有更多了</text>
      </view>

      <!-- Loading More -->
      <view v-if="loading && reviews.length > 0" class="loading-more">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getToolReviews, markHelpful, unmarkHelpful, deleteReview, getRatingDistribution } from '../../api/review'
import type { Review, RatingDistribution } from '../../api/review'

const reviews = ref<Review[]>([])
const ratingDistribution = ref<RatingDistribution | null>(null)
const loading = ref(false)
const sortBy = ref('latest')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const toolId = ref<number>(0)
const currentUserId = ref<number | null>(null)

const defaultAvatar = 'https://via.placeholder.com/60'

const hasMore = computed(() => {
  return reviews.value.length < total.value
})

onLoad((options: any) => {
  if (options.toolId) {
    toolId.value = Number(options.toolId)
    loadRatingDistribution()
    loadReviews()
  }

  // Get current user ID from storage
  const userInfo = uni.getStorageSync('userInfo')
  if (userInfo && userInfo.id) {
    currentUserId.value = userInfo.id
  }
})

const loadRatingDistribution = async () => {
  try {
    const distribution = await getRatingDistribution(toolId.value)
    ratingDistribution.value = distribution
  } catch (error) {
    console.error('加载评分分布失败:', error)
  }
}

const loadReviews = async () => {
  try {
    loading.value = true

    const result = await getToolReviews(toolId.value, currentPage.value, pageSize.value)

    if (currentPage.value === 1) {
      reviews.value = result.records
    } else {
      reviews.value = [...reviews.value, ...result.records]
    }

    total.value = result.total
  } catch (error) {
    console.error('加载评论失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleSort = (sort: string) => {
  sortBy.value = sort
  currentPage.value = 1
  loadReviews()
}

const loadMore = () => {
  currentPage.value++
  loadReviews()
}

const handleBack = () => {
  uni.navigateBack()
}

const handleWriteReview = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }

  uni.navigateTo({
    url: `/pages/tool/write-review?toolId=${toolId.value}`
  })
}

const handleMarkHelpful = async (review: Review) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }

  try {
    if (review.hasMarkedHelpful) {
      await unmarkHelpful(review.id)
      review.hasMarkedHelpful = false
      review.helpfulCount--
    } else {
      await markHelpful(review.id)
      review.hasMarkedHelpful = true
      review.helpfulCount++
    }

    uni.showToast({
      title: review.hasMarkedHelpful ? '标记成功' : '已取消',
      icon: 'success'
    })
  } catch (error: any) {
    console.error('操作失败:', error)
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

const isMyReview = (review: Review) => {
  return currentUserId.value && review.userId === currentUserId.value
}

const handleEditReview = (review: Review) => {
  uni.navigateTo({
    url: `/pages/tool/write-review?toolId=${toolId.value}&reviewId=${review.id}`
  })
}

const handleDeleteReview = (review: Review) => {
  uni.showModal({
    title: '确认',
    content: '确定要删除这条评论吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteReview(review.id)

          // Remove from list
          reviews.value = reviews.value.filter(r => r.id !== review.id)
          total.value--

          // Reload rating distribution
          loadRatingDistribution()

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
        } catch (error: any) {
          console.error('删除失败:', error)
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

const getBarWidth = (count: number, total: number) => {
  if (total === 0) return '0%'
  return `${(count / total * 100).toFixed(1)}%`
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`

  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.back-btn {
  font-size: 48rpx;
  color: #333;
  font-weight: 300;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.write-btn {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
}

/* Summary Section */
.summary-section {
  background-color: #fff;
  padding: 40rpx;
  margin-bottom: 20rpx;
}

.average-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.rating-number {
  font-size: 72rpx;
  font-weight: bold;
  color: #667eea;
}

.stars {
  display: flex;
  gap: 8rpx;
}

.star {
  font-size: 28rpx;
}

.total-count {
  font-size: 24rpx;
  color: #999;
}

.rating-bars {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.rating-label {
  font-size: 24rpx;
  color: #666;
  width: 60rpx;
}

.bar-container {
  flex: 1;
  height: 12rpx;
  background-color: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
}

.rating-count {
  font-size: 24rpx;
  color: #999;
  width: 60rpx;
  text-align: right;
}

/* Sort Section */
.sort-section {
  display: flex;
  background-color: #fff;
  margin-bottom: 20rpx;
}

.sort-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #666;
  position: relative;
}

.sort-item.active {
  color: #667eea;
  font-weight: bold;
}

.sort-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2rpx;
}

/* Reviews List */
.reviews-list {
  padding: 0 40rpx 40rpx;
}

.review-item {
  padding: 30rpx;
  margin-bottom: 20rpx;
  background-color: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #e0e0e0;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.review-rating {
  display: flex;
  gap: 4rpx;
}

.review-date {
  font-size: 24rpx;
  color: #999;
}

.review-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  display: block;
  margin-bottom: 16rpx;
}

.review-pros-cons {
  margin-bottom: 12rpx;
}

.pros-cons-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #333;
  margin-right: 8rpx;
}

.pros-cons-content {
  font-size: 26rpx;
  color: #666;
}

.review-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1px solid #f5f5f5;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 20rpx;
  background-color: #f5f7fa;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
}

.action-btn.active {
  background-color: #e8eaf6;
  color: #667eea;
}

.action-btn.delete {
  color: #f44336;
}

.action-icon {
  font-size: 20rpx;
}

.action-count {
  margin-left: 4rpx;
  color: #999;
}

/* Loading & Empty States */
.loading,
.loading-more,
.no-more {
  text-align: center;
  padding: 60rpx 0;
  font-size: 28rpx;
  color: #999;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.btn-write {
  margin-top: 20rpx;
  padding: 16rpx 48rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 24rpx;
  font-size: 28rpx;
  border: none;
}

.load-more {
  text-align: center;
  padding: 40rpx 0;
}

.btn-load-more {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #f5f7fa;
  color: #333;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}
</style>
