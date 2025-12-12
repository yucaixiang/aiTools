<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-container">
        <div class="profile-header">
          <img :src="userStore.user?.avatar || '/default-avatar.png'" alt="avatar" class="avatar" />
          <div class="user-info">
            <h2>{{ userStore.user?.username || '未登录' }}</h2>
            <p>{{ userStore.user?.email }}</p>
          </div>
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>

        <div class="profile-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="tab-content">
          <!-- 我的收藏 -->
          <div v-if="activeTab === 'favorites'" class="favorites-list">
            <div v-if="favorites.length === 0" class="empty">
              <p>暂无收藏的工具</p>
            </div>
            <div v-else class="tools-grid">
              <div
                v-for="tool in favorites"
                :key="tool.id"
                class="tool-card"
                @click="goToTool(tool.id)"
              >
                <img :src="tool.logoUrl || '/default-logo.png'" alt="logo" />
                <div class="tool-info">
                  <h3>{{ tool.name }}</h3>
                  <p>{{ tool.tagline }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 我的评论 -->
          <div v-if="activeTab === 'reviews'" class="reviews-list">
            <div v-if="reviews.length === 0" class="empty">
              <p>暂无评论</p>
            </div>
            <div v-else>
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <h4>{{ review.toolName }}</h4>
                  <div class="rating">
                    <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= review.rating }">
                      ★
                    </span>
                  </div>
                </div>
                <p class="review-content">{{ review.content }}</p>
                <p class="review-date">{{ formatDate(review.createdAt) }}</p>
              </div>
            </div>
          </div>

          <!-- 提交的工具 -->
          <div v-if="activeTab === 'submitted'" class="submitted-list">
            <div v-if="submittedTools.length === 0" class="empty">
              <p>暂无提交的工具</p>
              <button @click="showSubmitForm = true" class="submit-btn">提交工具</button>
            </div>
            <div v-else class="tools-grid">
              <div
                v-for="tool in submittedTools"
                :key="tool.id"
                class="tool-card"
              >
                <img :src="tool.logoUrl || '/default-logo.png'" alt="logo" />
                <div class="tool-info">
                  <h3>{{ tool.name }}</h3>
                  <p>{{ tool.tagline }}</p>
                  <span :class="['status', tool.status]">
                    {{ tool.status === 'approved' ? '已通过' : tool.status === 'pending' ? '待审核' : '已拒绝' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyFavorites } from '@/api/favorite'
import { getMyReviews } from '@/api/review'
import { getMySubmissions } from '@/api/submission'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('favorites')
const favorites = ref([])
const reviews = ref([])
const submittedTools = ref([])

const tabs = [
  { key: 'favorites', label: '我的收藏' },
  { key: 'reviews', label: '我的评论' },
  { key: 'submitted', label: '提交的工具' }
]

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
  } else {
    loadData()
  }
})

watch(activeTab, () => {
  loadData()
})

async function loadData() {
  if (activeTab.value === 'favorites') {
    await loadFavorites()
  } else if (activeTab.value === 'reviews') {
    await loadReviews()
  } else if (activeTab.value === 'submitted') {
    await loadSubmissions()
  }
}

async function loadFavorites() {
  try {
    const res = await getMyFavorites({ current: 1, size: 20 })
    favorites.value = res.data?.records || []
  } catch (error) {
    console.error('加载收藏失败:', error)
    favorites.value = []
  }
}

async function loadReviews() {
  try {
    const res = await getMyReviews({ current: 1, size: 20 })
    reviews.value = res.data?.records || []
  } catch (error) {
    console.error('加载评论失败:', error)
    reviews.value = []
  }
}

async function loadSubmissions() {
  try {
    const res = await getMySubmissions({ current: 1, size: 20 })
    submittedTools.value = res.data?.records || []
  } catch (error) {
    console.error('加载提交失败:', error)
    submittedTools.value = []
  }
}

function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

function goToTool(id) {
  router.push(`/tool/${id}`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 40px 0;
  min-height: calc(100vh - 128px);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.profile-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  border-bottom: 1px solid #f0f0f0;

  .avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
  }

  .user-info {
    flex: 1;

    h2 {
      font-size: 24px;
      margin-bottom: 4px;
    }

    p {
      color: #666;
      font-size: 14px;
    }
  }

  .logout-btn {
    padding: 10px 24px;
    background: white;
    color: #ff4d4f;
    border: 2px solid #ff4d4f;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
    transition: all 0.3s;

    &:hover {
      background: #ff4d4f;
      color: white;
    }
  }
}

.profile-tabs {
  display: flex;
  border-bottom: 1px solid #f0f0f0;

  .tab-btn {
    flex: 1;
    padding: 16px;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 15px;
    color: #666;
    transition: all 0.3s;
    position: relative;

    &:hover {
      color: #1890ff;
    }

    &.active {
      color: #1890ff;
      font-weight: 500;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: #1890ff;
      }
    }
  }
}

.tab-content {
  padding: 32px;
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: #999;

  .submit-btn {
    margin-top: 20px;
    padding: 12px 32px;
    background: #1890ff;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 15px;
    transition: background 0.3s;

    &:hover {
      background: #40a9ff;
    }
  }
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;

  .tool-card {
    display: flex;
    gap: 12px;
    padding: 16px;
    background: #fafafa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f0f0f0;
    }

    img {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      object-fit: cover;
      flex-shrink: 0;
    }

    .tool-info {
      flex: 1;
      min-width: 0;

      h3 {
        font-size: 16px;
        margin-bottom: 4px;
      }

      p {
        font-size: 13px;
        color: #666;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .status {
        display: inline-block;
        margin-top: 4px;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;

        &.approved {
          background: #d4edda;
          color: #155724;
        }

        &.pending {
          background: #fff3cd;
          color: #856404;
        }

        &.rejected {
          background: #f8d7da;
          color: #721c24;
        }
      }
    }
  }
}

.reviews-list {
  .review-item {
    padding: 20px;
    background: #fafafa;
    border-radius: 8px;
    margin-bottom: 16px;

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      h4 {
        margin: 0;
        font-size: 16px;
      }

      .rating {
        .star {
          color: #ddd;
          font-size: 16px;

          &.active {
            color: #ffa940;
          }
        }
      }
    }

    .review-content {
      color: #333;
      line-height: 1.6;
      margin-bottom: 8px;
    }

    .review-date {
      font-size: 13px;
      color: #999;
    }
  }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }
}
</style>
