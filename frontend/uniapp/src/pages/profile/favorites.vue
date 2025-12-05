<template>
  <view class="container">
    <!-- Header -->
    <view class="header">
      <text class="back-btn" @click="handleBack">‹ 返回</text>
      <text class="header-title">我的收藏</text>
      <text class="count-text">{{ total }} 个</text>
    </view>

    <!-- Tools List -->
    <view class="tools-list">
      <!-- Loading State -->
      <view v-if="loading && tools.length === 0" class="loading">
        <text>加载中...</text>
      </view>

      <!-- Tool Items -->
      <view
        v-for="tool in tools"
        :key="tool.id"
        class="tool-item"
        @click="goToDetail(tool.id)"
      >
        <image class="tool-logo" :src="tool.logoUrl" mode="aspectFit" />
        <view class="tool-info">
          <view class="tool-header">
            <text class="tool-name">{{ tool.name }}</text>
            <view class="pricing-badge" :class="getPricingClass(tool.pricingModel)">
              <text>{{ getPricingText(tool.pricingModel) }}</text>
            </view>
          </view>

          <text class="tool-tagline">{{ tool.tagline }}</text>

          <view class="tool-tags">
            <view class="tag" v-for="tag in tool.tags.slice(0, 3)" :key="tag.id">
              <text class="tag-text">{{ tag.name }}</text>
            </view>
          </view>

          <view class="tool-stats">
            <view class="stat-item">
              <text class="stat-icon">⭐</text>
              <text class="stat-value">{{ tool.averageRating }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">💬</text>
              <text class="stat-value">{{ tool.reviewCount }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">👍</text>
              <text class="stat-value">{{ tool.upvoteCount }}</text>
            </view>
          </view>
        </view>

        <view class="tool-actions">
          <view class="action-btn unfavorite" @click.stop="handleUnfavorite(tool)">
            <text class="action-icon">❤️</text>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-if="!loading && tools.length === 0" class="empty">
        <text class="empty-icon">📦</text>
        <text class="empty-text">还没有收藏任何工具</text>
        <button class="btn-explore" @click="goToExplore">去发现</button>
      </view>

      <!-- Load More -->
      <view v-if="!loading && hasMore" class="load-more">
        <button class="btn-load-more" @click="loadMore">加载更多</button>
      </view>

      <!-- No More -->
      <view v-if="!loading && tools.length > 0 && !hasMore" class="no-more">
        <text>没有更多了</text>
      </view>

      <!-- Loading More -->
      <view v-if="loading && tools.length > 0" class="loading-more">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import type { Tool } from '../../api/tool'

const tools = ref<Tool[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const hasMore = computed(() => {
  return tools.value.length < total.value
})

onLoad(() => {
  loadFavorites()
})

const loadFavorites = async () => {
  // TODO: Implement API call to get user favorites
  try {
    loading.value = true

    // Mock data for now
    await new Promise(resolve => setTimeout(resolve, 1000))

    // In real implementation:
    // const result = await getUserFavorites(currentPage.value, pageSize.value)
    // tools.value = currentPage.value === 1 ? result.records : [...tools.value, ...result.records]
    // total.value = result.total

    tools.value = []
    total.value = 0
  } catch (error) {
    console.error('加载收藏失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  currentPage.value++
  loadFavorites()
}

const handleBack = () => {
  uni.navigateBack()
}

const goToDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/tool/detail?id=${id}`
  })
}

const goToExplore = () => {
  uni.switchTab({
    url: '/pages/explore/index'
  })
}

const handleUnfavorite = async (tool: Tool) => {
  uni.showModal({
    title: '确认',
    content: '确定要取消收藏吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: Call API to unfavorite
          // await cancelFavorite(tool.id)

          // Remove from list
          tools.value = tools.value.filter(t => t.id !== tool.id)
          total.value--

          uni.showToast({
            title: '已取消收藏',
            icon: 'success'
          })
        } catch (error: any) {
          console.error('取消收藏失败:', error)
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

const getPricingClass = (model: string) => {
  const classMap: Record<string, string> = {
    'FREE': 'pricing-free',
    'FREEMIUM': 'pricing-freemium',
    'PAID': 'pricing-paid',
    'OPEN_SOURCE': 'pricing-opensource'
  }
  return classMap[model] || ''
}

const getPricingText = (model: string) => {
  const textMap: Record<string, string> = {
    'FREE': '免费',
    'FREEMIUM': '免费增值',
    'PAID': '付费',
    'OPEN_SOURCE': '开源'
  }
  return textMap[model] || model
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

.back-btn {
  font-size: 48rpx;
  color: #333;
  font-weight: 300;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
  flex: 1;
  text-align: center;
}

.count-text {
  font-size: 26rpx;
  color: #999;
}

/* Tools List */
.tools-list {
  padding: 0 40rpx;
}

.tool-item {
  display: flex;
  gap: 24rpx;
  padding: 30rpx;
  margin-top: 20rpx;
  background-color: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  position: relative;
}

.tool-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

.tool-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tool-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.tool-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
  flex: 1;
}

.pricing-badge {
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.pricing-free {
  background-color: #e8f5e9;
  color: #4caf50;
}

.pricing-freemium {
  background-color: #e3f2fd;
  color: #2196f3;
}

.pricing-paid {
  background-color: #fff3e0;
  color: #ff9800;
}

.pricing-opensource {
  background-color: #f3e5f5;
  color: #9c27b0;
}

.tool-tagline {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tool-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.tag {
  padding: 4rpx 16rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
}

.tag-text {
  font-size: 22rpx;
  color: #666;
}

.tool-stats {
  display: flex;
  gap: 32rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #999;
}

.stat-icon {
  font-size: 20rpx;
}

.stat-value {
  font-weight: 500;
  color: #666;
}

.tool-actions {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
}

.action-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff3e0;
  border-radius: 50%;
}

.action-btn.unfavorite {
  background-color: #ffebee;
}

.action-icon {
  font-size: 32rpx;
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

.btn-explore {
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
