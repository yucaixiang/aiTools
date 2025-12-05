<template>
  <view class="container">
    <!-- User Info Section -->
    <view class="user-section">
      <view v-if="isLoggedIn" class="user-info">
        <image class="user-avatar" :src="userInfo.avatar || defaultAvatar" mode="aspectFill" />
        <view class="user-details">
          <text class="user-name">{{ userInfo.username }}</text>
          <text class="user-email">{{ userInfo.email }}</text>
        </view>
      </view>

      <view v-else class="login-prompt">
        <text class="prompt-text">登录后体验更多功能</text>
        <button class="btn-login" @click="goToLogin">立即登录</button>
      </view>
    </view>

    <!-- Stats Section -->
    <view v-if="isLoggedIn" class="stats-section">
      <view class="stat-item" @click="goToFavorites">
        <text class="stat-value">{{ stats.favoriteCount }}</text>
        <text class="stat-label">收藏</text>
      </view>
      <view class="stat-item" @click="goToReviews">
        <text class="stat-value">{{ stats.reviewCount }}</text>
        <text class="stat-label">评论</text>
      </view>
      <view class="stat-item" @click="goToUpvotes">
        <text class="stat-value">{{ stats.upvoteCount }}</text>
        <text class="stat-label">点赞</text>
      </view>
    </view>

    <!-- Menu List -->
    <view class="menu-section">
      <view v-if="isLoggedIn" class="menu-group">
        <text class="group-title">我的内容</text>
        <view class="menu-item" @click="goToFavorites">
          <view class="menu-left">
            <text class="menu-icon">❤️</text>
            <text class="menu-label">我的收藏</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToReviews">
          <view class="menu-left">
            <text class="menu-icon">💬</text>
            <text class="menu-label">我的评论</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToUpvotes">
          <view class="menu-left">
            <text class="menu-icon">👍</text>
            <text class="menu-label">我的点赞</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-group">
        <text class="group-title">应用设置</text>
        <view class="menu-item" @click="goToAbout">
          <view class="menu-left">
            <text class="menu-icon">ℹ️</text>
            <text class="menu-label">关于我们</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="showFeedback">
          <view class="menu-left">
            <text class="menu-icon">📝</text>
            <text class="menu-label">意见反馈</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="clearCache">
          <view class="menu-left">
            <text class="menu-icon">🗑️</text>
            <text class="menu-label">清除缓存</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view v-if="isLoggedIn" class="menu-group">
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </view>
    </view>

    <!-- Version Info -->
    <view class="version-info">
      <text class="version-text">版本号: v1.0.0</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getCurrentUser } from '../../api/user'

interface UserInfo {
  id: number
  username: string
  email: string
  avatar: string
  role: string
}

interface Stats {
  favoriteCount: number
  reviewCount: number
  upvoteCount: number
}

const userInfo = ref<UserInfo | null>(null)
const stats = ref<Stats>({
  favoriteCount: 0,
  reviewCount: 0,
  upvoteCount: 0
})

const defaultAvatar = 'https://via.placeholder.com/120'

const isLoggedIn = computed(() => {
  const token = uni.getStorageSync('token')
  return !!token && !!userInfo.value
})

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  const token = uni.getStorageSync('token')
  if (!token) return

  try {
    const user = await getCurrentUser()
    userInfo.value = user

    // Load user stats
    // TODO: Implement stats API
    stats.value = {
      favoriteCount: user.favoriteCount || 0,
      reviewCount: user.reviewCount || 0,
      upvoteCount: user.upvoteCount || 0
    }
  } catch (error: any) {
    console.error('加载用户信息失败:', error)

    if (error.code === 401) {
      // Token expired
      uni.removeStorageSync('token')
      uni.removeStorageSync('refreshToken')
      userInfo.value = null
    }
  }
}

const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/login'
  })
}

const goToFavorites = () => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToReviews = () => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToUpvotes = () => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: 'AI工具推荐系统 v1.0.0\n\n一个基于AI的软件工具发现和推荐平台，帮助用户快速找到合适的AI工具。',
    showCancel: false
  })
}

const showFeedback = () => {
  uni.showModal({
    title: '意见反馈',
    content: '如有任何问题或建议，请发送邮件至：feedback@toolrecommend.com',
    showCancel: false
  })
}

const clearCache = () => {
  uni.showModal({
    title: '确认',
    content: '确定要清除缓存吗？',
    success: (res) => {
      if (res.confirm) {
        // Clear cache except token
        const token = uni.getStorageSync('token')
        const refreshToken = uni.getStorageSync('refreshToken')

        uni.clearStorageSync()

        if (token) {
          uni.setStorageSync('token', token)
          uni.setStorageSync('refreshToken', refreshToken)
        }

        uni.showToast({
          title: '清除成功',
          icon: 'success'
        })
      }
    }
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '确认',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // Clear all storage
        uni.clearStorageSync()

        // Reset user info
        userInfo.value = null
        stats.value = {
          favoriteCount: 0,
          reviewCount: 0,
          upvoteCount: 0
        }

        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })

        // Optional: Navigate to login page
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages/auth/login'
          })
        }, 1000)
      }
    }
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 40rpx;
}

/* User Section */
.user-section {
  padding: 60rpx 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background-color: #fff;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.user-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.user-email {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
  padding: 40rpx 0;
}

.prompt-text {
  font-size: 28rpx;
  color: #fff;
}

.btn-login {
  padding: 16rpx 60rpx;
  background-color: #fff;
  color: #667eea;
  border-radius: 48rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
}

/* Stats Section */
.stats-section {
  display: flex;
  margin: -60rpx 40rpx 0;
  background-color: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 40rpx 0;
  border-right: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-right: none;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* Menu Section */
.menu-section {
  margin-top: 40rpx;
  padding: 0 40rpx;
}

.menu-group {
  margin-bottom: 30rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.group-title {
  display: block;
  padding: 24rpx 30rpx 16rpx;
  font-size: 24rpx;
  font-weight: 500;
  color: #999;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.menu-icon {
  font-size: 32rpx;
  width: 48rpx;
  text-align: center;
}

.menu-label {
  font-size: 28rpx;
  color: #333;
}

.menu-arrow {
  font-size: 40rpx;
  color: #ccc;
  font-weight: 300;
}

.btn-logout {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #fff;
  color: #f44336;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  border-radius: 0;
}

/* Version Info */
.version-info {
  text-align: center;
  padding: 40rpx 0;
}

.version-text {
  font-size: 24rpx;
  color: #ccc;
}
</style>
