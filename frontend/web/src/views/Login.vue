<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <h2>登录</h2>
        <p class="subtitle">使用账号登录AI工具推荐</p>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="error-message">
          <span class="error-icon">⚠️</span>
          <span>{{ errorMessage }}</span>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>账号</label>
            <input
              v-model="loginForm.account"
              type="text"
              placeholder="请输入账号或邮箱"
              required
            />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="register-link">
          还没有账号？<a href="#" @click.prevent="showRegisterModal = true">立即注册</a>
        </div>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <div v-if="showRegisterModal" class="modal-overlay" @click="closeRegisterModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>注册新账号</h2>
          <button class="close-btn" @click="closeRegisterModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>

        <!-- 注册错误提示 -->
        <div v-if="registerError" class="error-message">
          <span class="error-icon">⚠️</span>
          <span>{{ registerError }}</span>
        </div>

        <!-- 成功提示 -->
        <div v-if="registerSuccess" class="success-message">
          <span class="success-icon">✓</span>
          <span>{{ registerSuccess }}</span>
        </div>

        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label>用户名</label>
            <input
              v-model="registerForm.username"
              type="text"
              placeholder="请输入用户名"
              required
            />
          </div>

          <div class="form-group">
            <label>邮箱 <span class="label-hint">（邮箱即为您的账号）</span></label>
            <input
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱，用于登录"
              required
            />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              required
              minlength="6"
            />
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <div class="modal-footer">
          已有账号？<a href="#" @click.prevent="closeRegisterModal">返回登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, register } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const showRegisterModal = ref(false)
const loading = ref(false)
const errorMessage = ref('')
const registerError = ref('')
const registerSuccess = ref('')

const loginForm = ref({
  account: '',
  password: ''
})

const registerForm = ref({
  username: '',
  email: '',
  password: ''
})

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    const res = await login(loginForm.value)
    userStore.setToken(res.data.accessToken)
    userStore.setUser(res.data.user)
    router.push('/')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  registerError.value = ''
  registerSuccess.value = ''
  loading.value = true

  try {
    await register(registerForm.value)
    registerSuccess.value = '注册成功！3秒后自动跳转到登录页面...'
    registerForm.value = { username: '', email: '', password: '' }

    // 3秒后关闭弹窗
    setTimeout(() => {
      closeRegisterModal()
    }, 3000)
  } catch (error) {
    const message = error.response?.data?.message || '注册失败，请稍后重试'
    registerError.value = message

    // 5秒后自动清除错误信息
    setTimeout(() => {
      registerError.value = ''
    }, 5000)
  } finally {
    loading.value = false
  }
}

function closeRegisterModal() {
  showRegisterModal.value = false
  registerError.value = ''
  registerSuccess.value = ''
  registerForm.value = { username: '', email: '', password: '' }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: calc(100vh - 128px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  h2 {
    font-size: 28px;
    margin-bottom: 8px;
    text-align: center;
  }

  .subtitle {
    text-align: center;
    color: #666;
    margin-bottom: 24px;
  }

  .form-group {
    margin-bottom: 20px;

    label {
      display: block;
      margin-bottom: 8px;
      color: #333;
      font-weight: 500;

      .label-hint {
        color: #999;
        font-size: 12px;
        font-weight: normal;
      }
    }

    input {
      width: 100%;
      padding: 12px 16px;
      border: 2px solid #e8e8e8;
      border-radius: 8px;
      font-size: 15px;
      transition: border-color 0.3s;

      &:focus {
        outline: none;
        border-color: #1890ff;
      }
    }
  }

  .login-btn {
    width: 100%;
    padding: 14px;
    background: #1890ff;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.3s;

    &:hover:not(:disabled) {
      background: #40a9ff;
    }

    &:disabled {
      background: #d9d9d9;
      cursor: not-allowed;
    }
  }

  .register-link {
    text-align: center;
    margin-top: 20px;
    color: #666;
    font-size: 14px;

    a {
      color: #1890ff;
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 错误提示样式
.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  color: #cf1322;
  font-size: 14px;
  margin-bottom: 20px;

  .error-icon {
    font-size: 18px;
  }
}

// 成功提示样式
.success-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  color: #52c41a;
  font-size: 14px;
  margin-bottom: 20px;

  .success-icon {
    font-size: 20px;
    font-weight: bold;
  }
}

// 弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 32px;
    border-bottom: 1px solid #f0f0f0;

    h2 {
      font-size: 24px;
      margin: 0;
      color: #333;
    }

    .close-btn {
      background: none;
      border: none;
      color: #999;
      cursor: pointer;
      padding: 4px;
      transition: all 0.3s;
      border-radius: 4px;

      &:hover {
        background: #f5f5f5;
        color: #333;
      }

      svg {
        display: block;
      }
    }
  }

  form {
    padding: 24px 32px;
  }

  .form-group {
    margin-bottom: 20px;

    label {
      display: block;
      margin-bottom: 8px;
      color: #333;
      font-weight: 500;

      .label-hint {
        color: #1890ff;
        font-size: 12px;
        font-weight: normal;
      }
    }

    input {
      width: 100%;
      padding: 12px 16px;
      border: 2px solid #e8e8e8;
      border-radius: 8px;
      font-size: 15px;
      transition: border-color 0.3s;

      &:focus {
        outline: none;
        border-color: #1890ff;
      }
    }
  }

  .submit-btn {
    width: 100%;
    padding: 14px;
    background: #1890ff;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.3s;

    &:hover:not(:disabled) {
      background: #40a9ff;
    }

    &:disabled {
      background: #d9d9d9;
      cursor: not-allowed;
    }
  }

  .modal-footer {
    text-align: center;
    padding: 0 32px 24px;
    color: #666;
    font-size: 14px;

    a {
      color: #1890ff;
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .login-box {
    padding: 32px 24px;
  }

  .modal-content {
    margin: 0;
    border-radius: 12px 12px 0 0;
    max-height: 95vh;

    .modal-header,
    form,
    .modal-footer {
      padding-left: 24px;
      padding-right: 24px;
    }
  }
}
</style>
