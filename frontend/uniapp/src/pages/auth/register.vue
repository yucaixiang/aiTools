<template>
  <view class="container">
    <!-- Header -->
    <view class="header">
      <text class="back-btn" @click="handleBack">‹ 返回</text>
    </view>

    <!-- Logo -->
    <view class="logo-section">
      <text class="logo-icon">🤖</text>
      <text class="logo-text">创建账号</text>
      <text class="subtitle">加入AI工具推荐社区</text>
    </view>

    <!-- Register Form -->
    <view class="form-section">
      <view class="input-group">
        <text class="input-label">用户名</text>
        <input
          class="input-field"
          :class="{ 'input-error': errors.username }"
          v-model="formData.username"
          placeholder="请输入用户名（3-20个字符）"
          :maxlength="20"
          @blur="validateUsername"
        />
        <text v-if="errors.username" class="error-text">{{ errors.username }}</text>
        <text v-if="!errors.username && usernameChecked" class="success-text">✓ 用户名可用</text>
      </view>

      <view class="input-group">
        <text class="input-label">邮箱</text>
        <input
          class="input-field"
          :class="{ 'input-error': errors.email }"
          v-model="formData.email"
          placeholder="请输入邮箱地址"
          :maxlength="50"
          @blur="validateEmail"
        />
        <text v-if="errors.email" class="error-text">{{ errors.email }}</text>
        <text v-if="!errors.email && emailChecked" class="success-text">✓ 邮箱可用</text>
      </view>

      <view class="input-group">
        <text class="input-label">密码</text>
        <view class="password-input">
          <input
            class="input-field"
            :class="{ 'input-error': errors.password }"
            v-model="formData.password"
            :password="!showPassword"
            placeholder="请输入密码（至少6个字符）"
            :maxlength="50"
            @blur="validatePassword"
          />
          <text class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '👁️' : '👁️‍🗨️' }}
          </text>
        </view>
        <text v-if="errors.password" class="error-text">{{ errors.password }}</text>

        <!-- Password Strength -->
        <view v-if="formData.password" class="password-strength">
          <view
            class="strength-bar"
            :class="'strength-' + passwordStrength.level"
            :style="{ width: passwordStrength.percent + '%' }"
          ></view>
          <text class="strength-text">{{ passwordStrength.text }}</text>
        </view>
      </view>

      <view class="input-group">
        <text class="input-label">确认密码</text>
        <view class="password-input">
          <input
            class="input-field"
            :class="{ 'input-error': errors.confirmPassword }"
            v-model="formData.confirmPassword"
            :password="!showConfirmPassword"
            placeholder="请再次输入密码"
            :maxlength="50"
            @blur="validateConfirmPassword"
          />
          <text class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
            {{ showConfirmPassword ? '👁️' : '👁️‍🗨️' }}
          </text>
        </view>
        <text v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</text>
      </view>

      <view class="agreement">
        <checkbox
          :checked="agreedToTerms"
          @click="agreedToTerms = !agreedToTerms"
          color="#667eea"
        />
        <text class="agreement-text" @click="agreedToTerms = !agreedToTerms">
          我已阅读并同意
          <text class="link" @click.stop="showTerms">《用户协议》</text>
          和
          <text class="link" @click.stop="showPrivacy">《隐私政策》</text>
        </text>
      </view>

      <button
        class="btn-register"
        :disabled="!canSubmit || isLoading"
        @click="handleRegister"
      >
        {{ isLoading ? '注册中...' : '注册' }}
      </button>

      <view class="login-link">
        <text class="login-text">已有账号？</text>
        <text class="login-btn" @click="goToLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { register, checkUsername, checkEmail } from '../../api/user'

const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errors = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreedToTerms = ref(false)
const isLoading = ref(false)
const usernameChecked = ref(false)
const emailChecked = ref(false)

const canSubmit = computed(() => {
  return (
    formData.value.username.trim() !== '' &&
    formData.value.email.trim() !== '' &&
    formData.value.password.trim() !== '' &&
    formData.value.confirmPassword.trim() !== '' &&
    agreedToTerms.value &&
    !errors.value.username &&
    !errors.value.email &&
    !errors.value.password &&
    !errors.value.confirmPassword
  )
})

const passwordStrength = computed(() => {
  const password = formData.value.password
  if (!password) return { level: 0, percent: 0, text: '' }

  let strength = 0
  if (password.length >= 6) strength++
  if (password.length >= 10) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/\d/.test(password)) strength++
  if (/[^a-zA-Z0-9]/.test(password)) strength++

  if (strength <= 1) return { level: 1, percent: 33, text: '弱' }
  if (strength <= 3) return { level: 2, percent: 66, text: '中等' }
  return { level: 3, percent: 100, text: '强' }
})

const validateUsername = async () => {
  const username = formData.value.username.trim()

  if (!username) {
    errors.value.username = '请输入用户名'
    usernameChecked.value = false
    return false
  }

  if (username.length < 3 || username.length > 20) {
    errors.value.username = '用户名长度为3-20个字符'
    usernameChecked.value = false
    return false
  }

  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    errors.value.username = '用户名只能包含字母、数字和下划线'
    usernameChecked.value = false
    return false
  }

  try {
    const available = await checkUsername(username)
    if (!available) {
      errors.value.username = '用户名已被使用'
      usernameChecked.value = false
      return false
    }

    errors.value.username = ''
    usernameChecked.value = true
    return true
  } catch (error) {
    console.error('检查用户名失败:', error)
    return false
  }
}

const validateEmail = async () => {
  const email = formData.value.email.trim()

  if (!email) {
    errors.value.email = '请输入邮箱'
    emailChecked.value = false
    return false
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    errors.value.email = '请输入有效的邮箱地址'
    emailChecked.value = false
    return false
  }

  try {
    const available = await checkEmail(email)
    if (!available) {
      errors.value.email = '邮箱已被使用'
      emailChecked.value = false
      return false
    }

    errors.value.email = ''
    emailChecked.value = true
    return true
  } catch (error) {
    console.error('检查邮箱失败:', error)
    return false
  }
}

const validatePassword = () => {
  const password = formData.value.password

  if (!password) {
    errors.value.password = '请输入密码'
    return false
  }

  if (password.length < 6) {
    errors.value.password = '密码长度至少6个字符'
    return false
  }

  errors.value.password = ''
  return true
}

const validateConfirmPassword = () => {
  const confirmPassword = formData.value.confirmPassword

  if (!confirmPassword) {
    errors.value.confirmPassword = '请确认密码'
    return false
  }

  if (confirmPassword !== formData.value.password) {
    errors.value.confirmPassword = '两次输入的密码不一致'
    return false
  }

  errors.value.confirmPassword = ''
  return true
}

const handleRegister = async () => {
  if (!canSubmit.value || isLoading.value) return

  // Validate all fields
  const usernameValid = await validateUsername()
  const emailValid = await validateEmail()
  const passwordValid = validatePassword()
  const confirmPasswordValid = validateConfirmPassword()

  if (!usernameValid || !emailValid || !passwordValid || !confirmPasswordValid) {
    return
  }

  try {
    isLoading.value = true

    const response = await register({
      username: formData.value.username,
      email: formData.value.email,
      password: formData.value.password
    })

    // Save tokens
    uni.setStorageSync('token', response.accessToken)
    uni.setStorageSync('refreshToken', response.refreshToken)

    // Save user info
    uni.setStorageSync('userInfo', response.user)

    uni.showToast({
      title: '注册成功',
      icon: 'success'
    })

    // Navigate to home
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 1000)
  } catch (error: any) {
    console.error('注册失败:', error)

    let errorMessage = '注册失败，请稍后重试'
    if (error.message) {
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
  uni.navigateBack()
}

const goToLogin = () => {
  uni.navigateBack()
}

const showTerms = () => {
  uni.showModal({
    title: '用户协议',
    content: '这里是用户协议的内容...',
    showCancel: false
  })
}

const showPrivacy = () => {
  uni.showModal({
    title: '隐私政策',
    content: '这里是隐私政策的内容...',
    showCancel: false
  })
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
  padding: 60rpx 0 40rpx;
}

.logo-icon {
  font-size: 100rpx;
  margin-bottom: 20rpx;
}

.logo-text {
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* Form Section */
.form-section {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx 60rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  margin-bottom: 60rpx;
}

.input-group {
  margin-bottom: 36rpx;
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

.input-error {
  border-color: #f44336 !important;
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

.error-text {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #f44336;
}

.success-text {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #4caf50;
}

/* Password Strength */
.password-strength {
  margin-top: 16rpx;
  position: relative;
  height: 8rpx;
  background-color: #f0f0f0;
  border-radius: 4rpx;
  overflow: hidden;
}

.strength-bar {
  height: 100%;
  transition: width 0.3s, background-color 0.3s;
}

.strength-1 {
  background-color: #f44336;
}

.strength-2 {
  background-color: #ff9800;
}

.strength-3 {
  background-color: #4caf50;
}

.strength-text {
  position: absolute;
  right: 0;
  top: -32rpx;
  font-size: 22rpx;
  color: #999;
}

/* Agreement */
.agreement {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 40rpx;
}

.agreement-text {
  flex: 1;
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

.link {
  color: #667eea;
}

.btn-register {
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

.btn-register[disabled] {
  background: #e0e0e0;
  color: #999;
}

.login-link {
  text-align: center;
}

.login-text {
  font-size: 26rpx;
  color: #999;
  margin-right: 8rpx;
}

.login-btn {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 500;
}
</style>
