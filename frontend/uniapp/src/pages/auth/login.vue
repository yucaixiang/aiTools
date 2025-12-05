<template>
  <view class="container">
    <!-- Header -->
    <view class="header">
      <text class="back-btn" @click="handleBack">‹ 返回</text>
    </view>

    <!-- Logo -->
    <view class="logo-section">
      <text class="logo-icon">🤖</text>
      <text class="logo-text">AI工具推荐</text>
      <text class="subtitle">发现最好的AI工具</text>
    </view>

    <!-- Login Form -->
    <view class="form-section">
      <view class="input-group">
        <text class="input-label">账号</text>
        <input
          class="input-field"
          v-model="formData.account"
          placeholder="请输入用户名或邮箱"
          :maxlength="50"
        />
      </view>

      <view class="input-group">
        <text class="input-label">密码</text>
        <view class="password-input">
          <input
            class="input-field"
            v-model="formData.password"
            :password="!showPassword"
            placeholder="请输入密码"
            :maxlength="50"
          />
          <text class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '👁️' : '👁️‍🗨️' }}
          </text>
        </view>
      </view>

      <view class="form-footer">
        <view class="remember-me">
          <checkbox
            :checked="rememberMe"
            @click="rememberMe = !rememberMe"
            color="#667eea"
          />
          <text class="remember-text" @click="rememberMe = !rememberMe">记住我</text>
        </view>
        <text class="forgot-password" @click="handleForgotPassword">忘记密码？</text>
      </view>

      <button
        class="btn-login"
        :disabled="!canSubmit || isLoading"
        @click="handleLogin"
      >
        {{ isLoading ? '登录中...' : '登录' }}
      </button>

      <view class="register-link">
        <text class="register-text">还没有账号？</text>
        <text class="register-btn" @click="goToRegister">立即注册</text>
      </view>
    </view>

    <!-- Divider -->
    <view class="divider">
      <view class="divider-line"></view>
      <text class="divider-text">或</text>
      <view class="divider-line"></view>
    </view>

    <!-- Social Login -->
    <view class="social-login">
      <view class="social-btn" @click="handleWechatLogin">
        <text class="social-icon">💬</text>
        <text class="social-text">微信登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { login } from '../../api/user'

const formData = ref({
  account: '',
  password: ''
})

const showPassword = ref(false)
const rememberMe = ref(false)
const isLoading = ref(false)

const canSubmit = computed(() => {
  return formData.value.account.trim() !== '' && formData.value.password.trim() !== ''
})

const handleLogin = async () => {
  if (!canSubmit.value || isLoading.value) return

  // Validate
  if (formData.value.account.length < 3) {
    uni.showToast({
      title: '账号长度至少3个字符',
      icon: 'none'
    })
    return
  }

  if (formData.value.password.length < 6) {
    uni.showToast({
      title: '密码长度至少6个字符',
      icon: 'none'
    })
    return
  }

  try {
    isLoading.value = true

    const response = await login({
      account: formData.value.account,
      password: formData.value.password
    })

    // Save tokens
    uni.setStorageSync('token', response.accessToken)
    uni.setStorageSync('refreshToken', response.refreshToken)

    // Save user info
    uni.setStorageSync('userInfo', response.user)

    // Save account if remember me
    if (rememberMe.value) {
      uni.setStorageSync('savedAccount', formData.value.account)
    } else {
      uni.removeStorageSync('savedAccount')
    }

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    // Navigate back or to home
    setTimeout(() => {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }
    }, 1000)
  } catch (error: any) {
    console.error('登录失败:', error)

    let errorMessage = '登录失败，请稍后重试'
    if (error.code === 1003) {
      errorMessage = '用户名或密码错误'
    } else if (error.message) {
      errorMessage = error.message
    }

    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  } finally {
    isLoading.value = false
  }
}

const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({
      url: '/pages/index/index'
    })
  }
}

const goToRegister = () => {
  uni.navigateTo({
    url: '/pages/auth/register'
  })
}

const handleForgotPassword = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleWechatLogin = () => {
  // #ifdef MP-WEIXIN
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
  // #endif

  // #ifndef MP-WEIXIN
  uni.showToast({
    title: '仅支持微信小程序',
    icon: 'none'
  })
  // #endif
}

// Load saved account on mount
const savedAccount = uni.getStorageSync('savedAccount')
if (savedAccount) {
  formData.value.account = savedAccount
  rememberMe.value = true
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 60rpx;
}

/* Header */
.header {
  padding: 40rpx 0;
}

.back-btn {
  font-size: 32rpx;
  color: #fff;
  font-weight: 500;
}

/* Logo Section */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 60rpx;
}

.logo-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.logo-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* Form Section */
.form-section {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.input-group {
  margin-bottom: 40rpx;
}

.input-label {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 16rpx;
}

.input-field {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  border: 2rpx solid transparent;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: #667eea;
  background-color: #fff;
}

.password-input {
  position: relative;
}

.password-input .input-field {
  padding-right: 80rpx;
}

.toggle-password {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 32rpx;
  cursor: pointer;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.remember-text {
  font-size: 26rpx;
  color: #666;
}

.forgot-password {
  font-size: 26rpx;
  color: #667eea;
}

.btn-login {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  margin-bottom: 30rpx;
}

.btn-login[disabled] {
  background: #e0e0e0;
  color: #999;
}

.register-link {
  text-align: center;
}

.register-text {
  font-size: 26rpx;
  color: #999;
  margin-right: 8rpx;
}

.register-btn {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 500;
}

/* Divider */
.divider {
  display: flex;
  align-items: center;
  padding: 60rpx 0 40rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background-color: rgba(255, 255, 255, 0.3);
}

.divider-text {
  padding: 0 24rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* Social Login */
.social-login {
  display: flex;
  justify-content: center;
  padding-bottom: 60rpx;
}

.social-btn {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 48rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 48rpx;
  backdrop-filter: blur(10rpx);
}

.social-icon {
  font-size: 32rpx;
}

.social-text {
  font-size: 28rpx;
  color: #fff;
}
</style>
