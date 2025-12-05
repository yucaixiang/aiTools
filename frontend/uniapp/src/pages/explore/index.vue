<template>
  <view class="container">
    <!-- Search Bar -->
    <view class="search-section">
      <view class="search-bar">
        <input
          class="search-input"
          placeholder="搜索AI工具..."
          v-model="searchKeyword"
          @confirm="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </view>
      <view class="filter-btn" @click="showFilterPopup = true">
        <text class="filter-icon">🔍</text>
        <text>筛选</text>
      </view>
    </view>

    <!-- Filter Tags -->
    <scroll-view v-if="activeFilters.length > 0" class="active-filters" scroll-x>
      <view class="filter-tags">
        <view class="filter-tag" v-for="(filter, index) in activeFilters" :key="index">
          <text>{{ filter.label }}</text>
          <text class="remove-tag" @click="removeFilter(filter)">×</text>
        </view>
        <view class="clear-all" @click="clearAllFilters">
          <text>清除全部</text>
        </view>
      </view>
    </scroll-view>

    <!-- Sort Options -->
    <view class="sort-section">
      <view
        class="sort-item"
        :class="{ active: sortBy === 'hot' }"
        @click="handleSort('hot')"
      >
        <text>最热门</text>
      </view>
      <view
        class="sort-item"
        :class="{ active: sortBy === 'latest' }"
        @click="handleSort('latest')"
      >
        <text>最新</text>
      </view>
      <view
        class="sort-item"
        :class="{ active: sortBy === 'rating' }"
        @click="handleSort('rating')"
      >
        <text>评分最高</text>
      </view>
      <view
        class="sort-item"
        :class="{ active: sortBy === 'reviews' }"
        @click="handleSort('reviews')"
      >
        <text>评论最多</text>
      </view>
    </view>

    <!-- Tool List -->
    <view class="tool-list">
      <!-- Loading State -->
      <view v-if="loading && toolList.length === 0" class="loading">
        <text>加载中...</text>
      </view>

      <!-- Tool Items -->
      <view
        v-for="tool in toolList"
        :key="tool.id"
        class="tool-item"
        @click="goToDetail(tool.id)"
      >
        <image class="tool-logo" :src="tool.logoUrl" mode="aspectFit" />
        <view class="tool-info">
          <view class="tool-header">
            <text class="tool-name">{{ tool.name }}</text>
            <view class="tool-pricing">
              <text class="pricing-badge" :class="getPricingClass(tool.pricingModel)">
                {{ getPricingText(tool.pricingModel) }}
              </text>
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
            <view class="stat-item">
              <text class="stat-icon">👁️</text>
              <text class="stat-value">{{ formatCount(tool.viewCount) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-if="!loading && toolList.length === 0" class="empty">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">未找到相关工具</text>
        <button class="btn-reset" @click="handleReset">重置筛选</button>
      </view>

      <!-- Load More -->
      <view v-if="!loading && hasMore" class="load-more">
        <button class="btn-load-more" @click="loadMore">加载更多</button>
      </view>

      <!-- No More -->
      <view v-if="!loading && toolList.length > 0 && !hasMore" class="no-more">
        <text>没有更多了</text>
      </view>

      <!-- Loading More -->
      <view v-if="loading && toolList.length > 0" class="loading-more">
        <text>加载中...</text>
      </view>
    </view>

    <!-- Filter Popup -->
    <view v-if="showFilterPopup" class="popup-overlay" @click="showFilterPopup = false">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">筛选工具</text>
          <text class="popup-close" @click="showFilterPopup = false">×</text>
        </view>

        <scroll-view class="popup-body" scroll-y>
          <!-- Category Filter -->
          <view class="filter-group">
            <text class="filter-group-title">分类</text>
            <view class="filter-options">
              <view
                class="filter-option"
                :class="{ active: selectedCategory === category.id }"
                v-for="category in categories"
                :key="category.id"
                @click="selectCategory(category.id)"
              >
                <text>{{ category.name }}</text>
              </view>
            </view>
          </view>

          <!-- Pricing Model Filter -->
          <view class="filter-group">
            <text class="filter-group-title">定价模式</text>
            <view class="filter-options">
              <view
                class="filter-option"
                :class="{ active: selectedPricing === pricing.value }"
                v-for="pricing in pricingOptions"
                :key="pricing.value"
                @click="selectPricing(pricing.value)"
              >
                <text>{{ pricing.label }}</text>
              </view>
            </view>
          </view>

          <!-- Rating Filter -->
          <view class="filter-group">
            <text class="filter-group-title">评分</text>
            <view class="filter-options">
              <view
                class="filter-option"
                :class="{ active: selectedRating === rating.value }"
                v-for="rating in ratingOptions"
                :key="rating.value"
                @click="selectRating(rating.value)"
              >
                <text>{{ rating.label }}</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="popup-footer">
          <button class="btn btn-secondary" @click="handleResetFilters">重置</button>
          <button class="btn btn-primary" @click="handleApplyFilters">应用</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { queryTools, searchTools } from '../../api/tool'
import type { Tool, PageResult } from '../../api/tool'

const toolList = ref<Tool[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const sortBy = ref('hot')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const showFilterPopup = ref(false)

// Filter options
const selectedCategory = ref<number | null>(null)
const selectedPricing = ref<string | null>(null)
const selectedRating = ref<number | null>(null)

const categories = ref([
  { id: 1, name: 'AI对话' },
  { id: 2, name: 'AI绘画' },
  { id: 3, name: 'AI写作' },
  { id: 4, name: 'AI编程' },
  { id: 5, name: '生产力' },
  { id: 6, name: '设计工具' }
])

const pricingOptions = [
  { value: 'FREE', label: '完全免费' },
  { value: 'FREEMIUM', label: '免费增值' },
  { value: 'PAID', label: '付费' },
  { value: 'OPEN_SOURCE', label: '开源' }
]

const ratingOptions = [
  { value: 4.5, label: '4.5星及以上' },
  { value: 4.0, label: '4.0星及以上' },
  { value: 3.5, label: '3.5星及以上' },
  { value: 3.0, label: '3.0星及以上' }
]

const hasMore = computed(() => {
  return toolList.value.length < total.value
})

const activeFilters = computed(() => {
  const filters: Array<{ type: string; label: string }> = []

  if (selectedCategory.value) {
    const category = categories.value.find(c => c.id === selectedCategory.value)
    if (category) {
      filters.push({ type: 'category', label: category.name })
    }
  }

  if (selectedPricing.value) {
    const pricing = pricingOptions.find(p => p.value === selectedPricing.value)
    if (pricing) {
      filters.push({ type: 'pricing', label: pricing.label })
    }
  }

  if (selectedRating.value) {
    const rating = ratingOptions.find(r => r.value === selectedRating.value)
    if (rating) {
      filters.push({ type: 'rating', label: rating.label })
    }
  }

  return filters
})

onLoad((options: any) => {
  if (options.keyword) {
    searchKeyword.value = options.keyword
  }
  if (options.category) {
    selectedCategory.value = Number(options.category)
  }
  loadTools()
})

const loadTools = async () => {
  try {
    loading.value = true

    let result: PageResult<Tool>

    // Build query params
    const params: any = {
      current: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }

    if (selectedPricing.value) {
      params.pricingModel = selectedPricing.value
    }

    if (selectedRating.value) {
      params.minRating = selectedRating.value
    }

    result = await queryTools(params)

    if (currentPage.value === 1) {
      toolList.value = result.records
    } else {
      toolList.value = [...toolList.value, ...result.records]
    }

    total.value = result.total
  } catch (error) {
    console.error('加载工具列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadTools()
}

const handleSort = (sort: string) => {
  sortBy.value = sort
  currentPage.value = 1
  loadTools()
}

const loadMore = () => {
  currentPage.value++
  loadTools()
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedCategory.value = null
  selectedPricing.value = null
  selectedRating.value = null
  currentPage.value = 1
  sortBy.value = 'hot'
  loadTools()
}

const removeFilter = (filter: { type: string; label: string }) => {
  switch (filter.type) {
    case 'category':
      selectedCategory.value = null
      break
    case 'pricing':
      selectedPricing.value = null
      break
    case 'rating':
      selectedRating.value = null
      break
  }
  currentPage.value = 1
  loadTools()
}

const clearAllFilters = () => {
  selectedCategory.value = null
  selectedPricing.value = null
  selectedRating.value = null
  currentPage.value = 1
  loadTools()
}

const selectCategory = (categoryId: number) => {
  selectedCategory.value = selectedCategory.value === categoryId ? null : categoryId
}

const selectPricing = (pricing: string) => {
  selectedPricing.value = selectedPricing.value === pricing ? null : pricing
}

const selectRating = (rating: number) => {
  selectedRating.value = selectedRating.value === rating ? null : rating
}

const handleResetFilters = () => {
  selectedCategory.value = null
  selectedPricing.value = null
  selectedRating.value = null
}

const handleApplyFilters = () => {
  showFilterPopup.value = false
  currentPage.value = 1
  loadTools()
}

const goToDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/tool/detail?id=${id}`
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

const formatCount = (count: number) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count.toString()
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 40rpx;
}

/* Search Section */
.search-section {
  display: flex;
  gap: 20rpx;
  padding: 30rpx 40rpx;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.search-bar {
  flex: 1;
  display: flex;
  gap: 16rpx;
}

.search-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  background-color: #f5f7fa;
  border-radius: 36rpx;
  font-size: 28rpx;
}

.search-btn {
  height: 72rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 36rpx;
  font-size: 28rpx;
  border: none;
  line-height: 72rpx;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  height: 72rpx;
  padding: 0 24rpx;
  background-color: #f5f7fa;
  border-radius: 36rpx;
  font-size: 28rpx;
  color: #333;
}

.filter-icon {
  font-size: 24rpx;
}

/* Active Filters */
.active-filters {
  white-space: nowrap;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 20rpx 0;
}

.filter-tags {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 40rpx;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  padding: 8rpx 20rpx;
  background-color: #e8eaf6;
  color: #667eea;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.remove-tag {
  font-size: 32rpx;
  font-weight: bold;
}

.clear-all {
  padding: 8rpx 20rpx;
  background-color: #ffebee;
  color: #f44336;
  border-radius: 20rpx;
  font-size: 24rpx;
}

/* Sort Section */
.sort-section {
  display: flex;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
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

/* Tool List */
.tool-list {
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
}

.tool-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
  flex: 1;
}

.tool-pricing {
  margin-left: 16rpx;
}

.pricing-badge {
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
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
  margin-top: 8rpx;
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

/* Loading & Empty States */
.loading,
.loading-more,
.no-more,
.empty {
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

.btn-reset {
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

/* Filter Popup */
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.popup-content {
  width: 100%;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.popup-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.popup-body {
  flex: 1;
  padding: 40rpx;
}

.filter-group {
  margin-bottom: 40rpx;
}

.filter-group-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 24rpx;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.filter-option {
  padding: 12rpx 28rpx;
  background-color: #f5f7fa;
  color: #666;
  border-radius: 24rpx;
  font-size: 26rpx;
  border: 2rpx solid transparent;
}

.filter-option.active {
  background-color: #e8eaf6;
  color: #667eea;
  border-color: #667eea;
}

.popup-footer {
  display: flex;
  gap: 20rpx;
  padding: 40rpx;
  border-top: 1px solid #f0f0f0;
}

.btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-secondary {
  background-color: #f5f7fa;
  color: #333;
}
</style>
