<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container">
        <div class="header-content">
          <router-link to="/" class="logo">
            <span class="logo-icon">🤖</span>
            <span class="logo-text">AI工具推荐</span>
          </router-link>

          <nav class="nav">
            <router-link to="/" class="nav-link">首页</router-link>
            <router-link to="/chat" class="nav-link">AI助手</router-link>
            <router-link to="/submit" class="nav-link submit-link">提交工具</router-link>
          </nav>

          <div class="header-right">
            <div v-if="userStore.isLoggedIn" class="user-menu">
              <router-link to="/profile" class="user-avatar">
                <img :src="userStore.user.avatar || '/default-avatar.png'" alt="avatar" />
              </router-link>
            </div>
            <router-link v-else to="/login" class="login-btn">登录</router-link>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container">
        <p>&copy; 2025 AI工具推荐. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 64px;
  }

  .logo {
    display: flex;
    align-items: center;
    text-decoration: none;
    font-size: 20px;
    font-weight: 600;
    color: #333;
    transition: opacity 0.3s;

    &:hover {
      opacity: 0.8;
    }

    .logo-icon {
      font-size: 28px;
      margin-right: 8px;
    }
  }

  .nav {
    display: flex;
    gap: 32px;

    .nav-link {
      text-decoration: none;
      color: #666;
      font-size: 15px;
      transition: color 0.3s;
      position: relative;

      &:hover {
        color: #1890ff;
      }

      &.router-link-active {
        color: #1890ff;
        font-weight: 500;

        &::after {
          content: '';
          position: absolute;
          bottom: -20px;
          left: 0;
          right: 0;
          height: 2px;
          background: #1890ff;
        }
      }
    }
  }

  .header-right {
    .user-avatar {
      display: block;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      overflow: hidden;
      border: 2px solid #f0f0f0;
      transition: border-color 0.3s;

      &:hover {
        border-color: #1890ff;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .login-btn {
      padding: 8px 20px;
      background: #1890ff;
      color: #fff;
      text-decoration: none;
      border-radius: 4px;
      font-size: 14px;
      transition: background 0.3s;

      &:hover {
        background: #40a9ff;
      }
    }
  }
}

.main {
  flex: 1;
}

.footer {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 24px 0;
  margin-top: 60px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    text-align: center;
    color: #999;
    font-size: 14px;
  }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .header {
    .nav {
      gap: 16px;

      .nav-link {
        font-size: 14px;
      }
    }

    .logo-text {
      display: none;
    }
  }
}
</style>
