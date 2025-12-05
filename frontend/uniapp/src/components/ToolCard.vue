<template>
  <view class="tool-card" @click="handleClick">
    <image class="tool-logo" :src="tool.logoUrl" mode="aspectFit" />
    <view class="tool-info">
      <view class="tool-header">
        <text class="tool-name">{{ tool.name }}</text>
        <view class="pricing-badge" :class="getPricingClass(tool.pricingModel)">
          <text>{{ getPricingText(tool.pricingModel) }}</text>
        </view>
      </view>

      <text class="tool-tagline">{{ tool.tagline }}</text>

      <view v-if="showTags && tool.tags && tool.tags.length > 0" class="tool-tags">
        <view class="tag" v-for="tag in tool.tags.slice(0, 3)" :key="tag.id">
          <text class="tag-text">{{ tag.name }}</text>
        </view>
      </view>

      <view class="tool-stats">
        <view class="stat-item">
          <text class="stat-icon">⭐</text>
          <text class="stat-value">{{ tool.averageRating || 0 }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-icon">💬</text>
          <text class="stat-value">{{ tool.reviewCount || 0 }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-icon">👍</text>
          <text class="stat-value">{{ tool.upvoteCount || 0 }}</text>
        </view>
        <view v-if="showViews" class="stat-item">
          <text class="stat-icon">👁️</text>
          <text class="stat-value">{{ formatCount(tool.viewCount || 0) }}</text>
        </view>
      </view>
    </view>

    <slot name="actions"></slot>
  </view>
</template>

<script setup lang="ts">
import type { Tool } from '../api/tool'

interface Props {
  tool: Tool
  showTags?: boolean
  showViews?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showTags: true,
  showViews: false
})

const emit = defineEmits<{
  click: [tool: Tool]
}>()

const handleClick = () => {
  emit('click', props.tool)
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
.tool-card {
  display: flex;
  gap: 24rpx;
  padding: 30rpx;
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
</style>
