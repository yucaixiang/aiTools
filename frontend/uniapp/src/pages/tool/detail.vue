<template>
  <view class="container">
    <!-- Loading State -->
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <!-- Tool Detail Content -->
    <view v-else-if="tool" class="detail-content">
      <!-- Header Section -->
      <view class="header">
        <image class="tool-logo" :src="tool.logoUrl" mode="aspectFit" />
        <view class="header-info">
          <text class="tool-name">{{ tool.name }}</text>
          <text class="tool-tagline">{{ tool.tagline }}</text>
          <view class="tool-meta">
            <view class="rating">
              <text class="star">⭐</text>
              <text class="rating-value">{{ tool.averageRating }}</text>
              <text class="rating-count">({{ tool.reviewCount }} 评论)</text>
            </view>
            <view class="upvotes">
              <text class="upvote-icon">👍</text>
              <text>{{ tool.upvoteCount }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Action Buttons -->
      <view class="action-buttons">
        <button
          class="btn btn-primary"
          :class="{ 'btn-upvoted': tool.hasUpvoted }"
          @click="handleUpvote"
        >
          {{ tool.hasUpvoted ? '已点赞' : '点赞' }}
        </button>
        <button
          class="btn btn-secondary"
          :class="{ 'btn-favorited': tool.hasFavorited }"
          @click="handleFavorite"
        >
          {{ tool.hasFavorited ? '已收藏' : '收藏' }}
        </button>
        <button class="btn btn-secondary" @click="handleVisitWebsite">
          访问官网
        </button>
      </view>

      <!-- Tags -->
      <view v-if="tool.tags && tool.tags.length > 0" class="tags-section">
        <view class="tag" v-for="tag in tool.tags" :key="tag.id">
          <text class="tag-text">{{ tag.name }}</text>
        </view>
      </view>

      <!-- Description -->
      <view class="section">
        <text class="section-title">产品介绍</text>
        <text class="description">{{ tool.description }}</text>
      </view>

      <!-- Pricing Info -->
      <view class="section">
        <text class="section-title">价格信息</text>
        <view class="pricing-info">
          <view class="pricing-item">
            <text class="pricing-label">定价模式：</text>
            <text class="pricing-value">{{ getPricingModelText(tool.pricingModel) }}</text>
          </view>
          <view v-if="tool.pricingDetails" class="pricing-item">
            <text class="pricing-label">详细价格：</text>
            <text class="pricing-value">{{ tool.pricingDetails }}</text>
          </view>
        </view>
      </view>

      <!-- Features -->
      <view v-if="tool.features && tool.features.length > 0" class="section">
        <text class="section-title">核心功能</text>
        <view class="features-list">
          <view class="feature-item" v-for="(feature, index) in tool.features" :key="index">
            <text class="feature-bullet">•</text>
            <text class="feature-text">{{ feature }}</text>
          </view>
        </view>
      </view>

      <!-- Rating Distribution -->
      <view v-if="ratingDistribution" class="section">
        <text class="section-title">评分分布</text>
        <view class="rating-distribution">
          <view
            class="rating-bar"
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
            <text class="rating-count-text">{{ item.count }}</text>
          </view>
        </view>
      </view>

      <!-- Hot Reviews -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">热门评论</text>
          <text class="view-all" @click="handleViewAllReviews">查看全部</text>
        </view>

        <view v-if="hotReviews && hotReviews.length > 0" class="reviews-list">
          <view class="review-item" v-for="review in hotReviews" :key="review.id">
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
              <text class="pros-cons-title">优点：</text>
              <text class="pros-cons-content">{{ review.pros.join('、') }}</text>
            </view>

            <view v-if="review.cons && review.cons.length > 0" class="review-pros-cons">
              <text class="pros-cons-title">缺点：</text>
              <text class="pros-cons-content">{{ review.cons.join('、') }}</text>
            </view>

            <view class="review-actions">
              <view class="helpful-btn" @click="handleMarkHelpful(review)">
                <text>{{ review.hasMarkedHelpful ? '已标记有帮助' : '有帮助' }}</text>
                <text class="helpful-count">({{ review.helpfulCount }})</text>
              </view>
            </view>
          </view>
        </view>

        <view v-else class="empty-reviews">
          <text>暂无评论，快来写第一条评论吧~</text>
        </view>

        <button class="btn btn-primary write-review-btn" @click="handleWriteReview">
          写评论
        </button>
      </view>

      <!-- Similar Tools -->
      <view v-if="similarTools && similarTools.length > 0" class="section">
        <text class="section-title">相似工具推荐</text>
        <scroll-view class="similar-tools-scroll" scroll-x>
          <view class="similar-tools-list">
            <view
              class="similar-tool-card"
              v-for="similarTool in similarTools"
              :key="similarTool.id"
              @click="handleGoToTool(similarTool.id)"
            >
              <image class="similar-tool-logo" :src="similarTool.logoUrl" mode="aspectFit" />
              <text class="similar-tool-name">{{ similarTool.name }}</text>
              <text class="similar-tool-tagline">{{ similarTool.tagline }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- Error State -->
    <view v-else class="error">
      <text>加载失败，请稍后重试</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getToolDetail,
  upvoteTool,
  favoriteTool,
  getSimilarTools
} from '../../api/tool'
import {
  getToolReviews,
  getHotReviews,
  markHelpful,
  getRatingDistribution
} from '../../api/review'
import type { ToolDetail, Review, RatingDistribution } from '../../api/tool'

const tool = ref<ToolDetail | null>(null)
const hotReviews = ref<Review[]>([])
const similarTools = ref<any[]>([])
const ratingDistribution = ref<RatingDistribution | null>(null)
const loading = ref(true)
const toolId = ref<number>(0)

const defaultAvatar = 'https://via.placeholder.com/40'

onLoad((options: any) => {
  if (options.id) {
    toolId.value = Number(options.id)
    loadToolDetail()
  }
})

const loadToolDetail = async () => {
  try {
    loading.value = true

    // Load tool detail
    const toolData = await getToolDetail(toolId.value)
    tool.value = toolData

    // Load hot reviews
    const reviews = await getHotReviews(toolId.value, 3)
    hotReviews.value = reviews

    // Load rating distribution
    const distribution = await getRatingDistribution(toolId.value)
    ratingDistribution.value = distribution

    // Load similar tools
    if (toolData.categoryId) {
      const similar = await getSimilarTools(toolId.value, 5)
      similarTools.value = similar
    }
  } catch (error) {
    console.error('加载工具详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleUpvote = async () => {
  if (!tool.value) return

  try {
    if (tool.value.hasUpvoted) {
      // TODO: Implement un-upvote
      uni.showToast({
        title: '已点赞',
        icon: 'none'
      })
      return
    }

    await upvoteTool(toolId.value)
    tool.value.hasUpvoted = true
    tool.value.upvoteCount++

    uni.showToast({
      title: '点赞成功',
      icon: 'success'
    })
  } catch (error: any) {
    if (error.code === 401) {
      uni.navigateTo({
        url: '/pages/auth/login'
      })
    } else {
      uni.showToast({
        title: '操作失败',
        icon: 'none'
      })
    }
  }
}

const handleFavorite = async () => {
  if (!tool.value) return

  try {
    if (tool.value.hasFavorited) {
      // TODO: Implement un-favorite
      uni.showToast({
        title: '已收藏',
        icon: 'none'
      })
      return
    }

    await favoriteTool(toolId.value)
    tool.value.hasFavorited = true

    uni.showToast({
      title: '收藏成功',
      icon: 'success'
    })
  } catch (error: any) {
    if (error.code === 401) {
      uni.navigateTo({
        url: '/pages/auth/login'
      })
    } else {
      uni.showToast({
        title: '操作失败',
        icon: 'none'
      })
    }
  }
}

const handleVisitWebsite = () => {
  if (!tool.value?.websiteUrl) return

  // H5 platform can open directly
  // #ifdef H5
  window.open(tool.value.websiteUrl, '_blank')
  // #endif

  // Mini-program needs to use web-view or copy link
  // #ifndef H5
  uni.setClipboardData({
    data: tool.value.websiteUrl,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    }
  })
  // #endif
}

const handleMarkHelpful = async (review: Review) => {
  if (review.hasMarkedHelpful) {
    uni.showToast({
      title: '已标记',
      icon: 'none'
    })
    return
  }

  try {
    await markHelpful(review.id)
    review.hasMarkedHelpful = true
    review.helpfulCount++

    uni.showToast({
      title: '标记成功',
      icon: 'success'
    })
  } catch (error: any) {
    if (error.code === 401) {
      uni.navigateTo({
        url: '/pages/auth/login'
      })
    } else {
      uni.showToast({
        title: error.message || '操作失败',
        icon: 'none'
      })
    }
  }
}

const handleViewAllReviews = () => {
  // TODO: Navigate to all reviews page
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleWriteReview = () => {
  // TODO: Navigate to write review page
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }

  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleGoToTool = (id: number) => {
  uni.redirectTo({
    url: `/pages/tool/detail?id=${id}`
  })
}

const getPricingModelText = (model: string) => {
  const map: Record<string, string> = {
    'FREE': '完全免费',
    'FREEMIUM': '免费增值',
    'PAID': '付费',
    'OPEN_SOURCE': '开源'
  }
  return map[model] || model
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
  padding-bottom: 40rpx;
}

.loading,
.error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  font-size: 28rpx;
  color: #999;
}

.detail-content {
  background-color: #fff;
}

/* Header Section */
.header {
  display: flex;
  padding: 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.tool-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 20rpx;
  margin-right: 30rpx;
  background-color: #f5f5f5;
}

.header-info {
  flex: 1;
}

.tool-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a1a;
  display: block;
  margin-bottom: 12rpx;
}

.tool-tagline {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 20rpx;
  line-height: 1.5;
}

.tool-meta {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.rating {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.star {
  font-size: 24rpx;
}

.rating-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.rating-count {
  font-size: 24rpx;
  color: #999;
}

.upvotes {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #666;
}

.upvote-icon {
  font-size: 24rpx;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 30rpx 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-upvoted {
  background: #e0e0e0;
  color: #666;
}

.btn-secondary {
  background-color: #f5f7fa;
  color: #333;
}

.btn-favorited {
  background-color: #fff3e0;
  color: #f57c00;
}

/* Tags */
.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 30rpx 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.tag {
  padding: 8rpx 20rpx;
  background-color: #f5f7fa;
  border-radius: 20rpx;
}

.tag-text {
  font-size: 24rpx;
  color: #666;
}

/* Section */
.section {
  padding: 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
  display: block;
  margin-bottom: 24rpx;
}

.view-all {
  font-size: 26rpx;
  color: #667eea;
}

.description {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  display: block;
}

/* Pricing */
.pricing-info {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.pricing-item {
  display: flex;
  font-size: 28rpx;
}

.pricing-label {
  color: #999;
  margin-right: 16rpx;
}

.pricing-value {
  color: #333;
  font-weight: 500;
}

/* Features */
.features-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.feature-item {
  display: flex;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.feature-bullet {
  margin-right: 12rpx;
  color: #667eea;
}

.feature-text {
  flex: 1;
}

/* Rating Distribution */
.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.rating-label {
  font-size: 26rpx;
  color: #666;
  width: 80rpx;
}

.bar-container {
  flex: 1;
  height: 16rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
}

.rating-count-text {
  font-size: 24rpx;
  color: #999;
  width: 60rpx;
  text-align: right;
}

/* Reviews */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  margin-bottom: 30rpx;
}

.review-item {
  padding: 30rpx;
  background-color: #f5f7fa;
  border-radius: 16rpx;
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
  justify-content: flex-end;
  margin-top: 16rpx;
}

.helpful-btn {
  padding: 8rpx 20rpx;
  background-color: #fff;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
}

.helpful-count {
  margin-left: 8rpx;
  color: #999;
}

.empty-reviews {
  text-align: center;
  padding: 60rpx 0;
  font-size: 28rpx;
  color: #999;
}

.write-review-btn {
  width: 100%;
  margin-top: 20rpx;
}

/* Similar Tools */
.similar-tools-scroll {
  white-space: nowrap;
  margin: 0 -40rpx;
  padding: 0 40rpx;
}

.similar-tools-list {
  display: inline-flex;
  gap: 20rpx;
}

.similar-tool-card {
  display: inline-block;
  width: 240rpx;
  padding: 24rpx;
  background-color: #f5f7fa;
  border-radius: 16rpx;
  vertical-align: top;
}

.similar-tool-logo {
  width: 100%;
  height: 120rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  background-color: #fff;
}

.similar-tool-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similar-tool-tagline {
  font-size: 24rpx;
  color: #999;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
