<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        placeholder="搜索AI工具..."
        v-model="searchKeyword"
        @confirm="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">搜索</button>
    </view>

    <!-- Banner -->
    <view class="banner">
      <text class="banner-title">发现最好的AI工具</text>
      <text class="banner-subtitle">让AI为您推荐最适合的软件工具</text>
    </view>

    <!-- 分类快捷入口 -->
    <view class="category-section">
      <text class="section-title">热门分类</text>
      <view class="category-grid">
        <view
          class="category-item"
          v-for="category in categories"
          :key="category.id"
          @click="goToCategory(category)"
        >
          <text class="category-icon">{{ category.icon }}</text>
          <text class="category-name">{{ category.name }}</text>
        </view>
      </view>
    </view>

    <!-- 热门工具 -->
    <view class="tool-section">
      <view class="section-header">
        <text class="section-title">热门工具</text>
        <text class="section-more" @click="goToExplore">查看更多 ></text>
      </view>
      <view class="tool-list">
        <view
          class="tool-card"
          v-for="tool in hotTools"
          :key="tool.id"
          @click="goToDetail(tool.id)"
        >
          <image class="tool-logo" :src="tool.logoUrl" mode="aspectFill" />
          <view class="tool-info">
            <text class="tool-name">{{ tool.name }}</text>
            <text class="tool-tagline">{{ tool.tagline }}</text>
            <view class="tool-meta">
              <text class="tool-rating">⭐ {{ tool.averageRating }}</text>
              <text class="tool-upvote">👍 {{ tool.upvoteCount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 最新工具 -->
    <view class="tool-section">
      <view class="section-header">
        <text class="section-title">最新工具</text>
      </view>
      <view class="tool-list">
        <view
          class="tool-card"
          v-for="tool in latestTools"
          :key="tool.id"
          @click="goToDetail(tool.id)"
        >
          <image class="tool-logo" :src="tool.logoUrl" mode="aspectFill" />
          <view class="tool-info">
            <text class="tool-name">{{ tool.name }}</text>
            <text class="tool-tagline">{{ tool.tagline }}</text>
            <view class="tool-tags">
              <text class="tag" v-for="tag in tool.tags.slice(0, 3)" :key="tag.id">
                {{ tag.name }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHotTools, getLatestTools } from '../../api/tool'
import type { Tool } from '../../api/tool'

const searchKeyword = ref('')
const hotTools = ref<Tool[]>([])
const latestTools = ref<Tool[]>([])

const categories = [
  { id: 1, name: '开发工具', icon: '💻' },
  { id: 2, name: '设计工具', icon: '🎨' },
  { id: 3, name: '生产力', icon: '📝' },
  { id: 4, name: '营销工具', icon: '📢' },
  { id: 5, name: 'AI工具', icon: '🤖' },
  { id: 6, name: '数据分析', icon: '📊' }
]

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    uni.showLoading({ title: '加载中...' })

    const [hot, latest] = await Promise.all([
      getHotTools(10),
      getLatestTools(10)
    ])

    hotTools.value = hot
    latestTools.value = latest
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    uni.hideLoading()
  }
}

function handleSearch() {
  if (!searchKeyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }

  uni.navigateTo({
    url: `/pages/explore/index?keyword=${searchKeyword.value}`
  })
}

function goToCategory(category: any) {
  uni.navigateTo({
    url: `/pages/explore/index?categoryId=${category.id}`
  })
}

function goToExplore() {
  uni.switchTab({
    url: '/pages/explore/index'
  })
}

function goToDetail(id: number) {
  uni.navigateTo({
    url: `/pages/tool/detail?id=${id}`
  })
}
</script>

<style scoped>
.container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #fff;
}

.search-input {
  flex: 1;
  height: 70rpx;
  padding: 0 20rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 35rpx;
  background-color: #f8f8f8;
}

.search-btn {
  margin-left: 15rpx;
  padding: 0 30rpx;
  height: 70rpx;
  line-height: 70rpx;
  background-color: #007aff;
  color: #fff;
  border-radius: 35rpx;
  font-size: 28rpx;
}

.banner {
  padding: 60rpx 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  text-align: center;
}

.banner-title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 15rpx;
}

.banner-subtitle {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.category-section {
  padding: 30rpx 20rpx;
  background-color: #fff;
  margin-top: 20rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25rpx;
  background-color: #f8f8f8;
  border-radius: 15rpx;
}

.category-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
}

.category-name {
  font-size: 26rpx;
  color: #666;
}

.tool-section {
  padding: 30rpx 20rpx;
  background-color: #fff;
  margin-top: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-more {
  font-size: 26rpx;
  color: #007aff;
}

.tool-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tool-card {
  display: flex;
  padding: 20rpx;
  background-color: #fafafa;
  border-radius: 15rpx;
}

.tool-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 15rpx;
  margin-right: 20rpx;
}

.tool-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.tool-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.tool-tagline {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-meta {
  display: flex;
  gap: 20rpx;
}

.tool-rating,
.tool-upvote {
  font-size: 24rpx;
  color: #999;
}

.tool-tags {
  display: flex;
  gap: 10rpx;
}

.tag {
  padding: 4rpx 12rpx;
  background-color: #e0e0e0;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #666;
}
</style>
