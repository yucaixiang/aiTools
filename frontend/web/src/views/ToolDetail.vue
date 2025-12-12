<template>
  <div class="tool-detail">
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-navigation">
        <button @click="goBack" class="back-btn">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"/>
          </svg>
          <span>返回列表</span>
        </button>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="tool" class="detail-content">
        <!-- 工具头部信息 -->
        <div class="tool-header">
          <img :src="tool.logoUrl || '/default-logo.png'" alt="logo" class="tool-logo" />
          <div class="tool-main-info">
            <h1 class="tool-name">{{ tool.name }}</h1>
            <p class="tool-tagline">{{ tool.tagline }}</p>

            <div class="tool-meta">
              <div class="meta-item">
                <span class="meta-icon">❤️</span>
                <span>{{ tool.favoriteCount || 0 }} 收藏人数</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">💬</span>
                <span>{{ tool.reviewCount || 0 }} 条评论</span>
              </div>
            </div>

            <!-- 评分组件 -->
            <RatingStars :tool-id="Number(route.params.id)" :interactive="true" @rated="handleRated" />

            <div class="tool-actions">
              <a :href="tool.websiteUrl" target="_blank" class="visit-btn">访问工具</a>
              <button @click="toggleFavorite" class="favorite-btn" :class="{ favorited: isFavorited }">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z"/>
                </svg>
                <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 工具详细描述 -->
        <div class="tool-description-section">
          <h2>工具介绍</h2>
          <div class="description-content" v-html="tool.description"></div>
        </div>

        <!-- 标签 -->
        <div v-if="tool.tags && tool.tags.length" class="tool-tags-section">
          <h3>标签</h3>
          <div class="tags-list">
            <span v-for="tag in tool.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="reviews-section">
          <div class="reviews-header">
            <h2>用户评论</h2>
            <button @click="showReviewForm = !showReviewForm" class="write-review-btn">
              写评论
            </button>
          </div>

          <!-- 写评论表单 -->
          <div v-if="showReviewForm" class="review-form">
            <textarea
              v-model="newReview.content"
              placeholder="分享你的使用体验..."
              rows="4"
              maxlength="2000"
            ></textarea>
            <div class="form-actions">
              <button @click="submitReview" class="submit-btn">提交评论</button>
              <button @click="showReviewForm = false" class="cancel-btn">取消</button>
            </div>
          </div>

          <!-- 评论树形列表 -->
          <ReviewTree
            ref="reviewTreeRef"
            :tool-id="Number(route.params.id)"
            @review-created="handleReviewCreated"
            @review-deleted="handleReviewDeleted"
          />
        </div>
      </div>

      <div v-else class="error">
        <p>工具不存在或已被删除</p>
        <router-link to="/" class="back-home">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToolDetail } from '@/api/tool'
import { createReview } from '@/api/review'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import toast from '@/utils/toast'
import RatingStars from '@/components/RatingStars.vue'
import ReviewTree from '@/components/ReviewTree.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const tool = ref(null)
const loading = ref(true)
const showReviewForm = ref(false)
const isFavorited = ref(false)
const reviewTreeRef = ref(null)

const newReview = ref({
  content: ''
})

// 评分后的处理
function handleRated(score) {
  console.log('用户评分:', score)
  // 重新加载工具详情以更新统计
  loadToolDetail()
}

// 评论创建后的处理
function handleReviewCreated() {
  // 重新加载工具详情以更新评论数
  loadToolDetail()
}

// 评论删除后的处理
function handleReviewDeleted() {
  // 重新加载工具详情以更新评论数
  loadToolDetail()
}

onMounted(async () => {
  await loadToolDetail()
  // 只有登录后才检查收藏状态
  if (userStore.isLoggedIn) {
    await checkIsFavorited()
  }
})

async function loadToolDetail() {
  loading.value = true
  try {
    const res = await getToolDetail(route.params.id)
    tool.value = res.data
  } catch (error) {
    console.error('加载工具详情失败:', error)
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!newReview.value.content.trim()) {
    toast.warning('请输入评论内容')
    return
  }

  try {
    await createReview({
      toolId: Number(route.params.id),
      content: newReview.value.content.trim()
    })
    toast.success('评论成功！')
    newReview.value.content = ''
    showReviewForm.value = false
    // 刷新评论树
    if (reviewTreeRef.value) {
      reviewTreeRef.value.loadReviews()
    }
    // 更新工具详情
    loadToolDetail()
  } catch (error) {
    // 错误已由响应拦截器自动显示toast
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (isFavorited.value) {
      await removeFavorite(route.params.id)
      isFavorited.value = false
      toast.success('已取消收藏')
    } else {
      await addFavorite(route.params.id)
      isFavorited.value = true
      toast.success('收藏成功')
    }
  } catch (error) {
    // 错误已由响应拦截器自动显示toast
  }
}

async function checkIsFavorited() {
  try {
    const res = await checkFavorite(route.params.id)
    isFavorited.value = res.data || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

function goBack() {
  router.push('/')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.tool-detail {
  padding: 20px 0 40px;
  min-height: calc(100vh - 80px);
  background: #f5f7fa;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.back-navigation {
  margin-bottom: 20px;

  .back-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: white;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
    color: #666;

    &:hover {
      border-color: #1890ff;
      color: #1890ff;
      background: #f0f8ff;
    }

    svg {
      transition: transform 0.3s;
    }

    &:hover svg {
      transform: translateX(-2px);
    }
  }
}

.loading,
.error {
  text-align: center;
  padding: 100px 0;

  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f0f0f0;
    border-top-color: #1890ff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
  }

  .back-home {
    display: inline-block;
    margin-top: 20px;
    padding: 10px 24px;
    background: #1890ff;
    color: white;
    text-decoration: none;
    border-radius: 4px;
  }
}

.detail-content {
  background: white;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.tool-header {
  display: flex;
  gap: 20px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;

  .tool-logo {
    width: 80px;
    height: 80px;
    border-radius: 12px;
    object-fit: cover;
    flex-shrink: 0;
  }

  .tool-main-info {
    flex: 1;

    .tool-name {
      font-size: 28px;
      font-weight: 700;
      margin-bottom: 6px;
      line-height: 1.3;
    }

    .tool-tagline {
      font-size: 15px;
      color: #666;
      margin-bottom: 12px;
      line-height: 1.5;
    }

    .tool-meta {
      display: flex;
      gap: 24px;
      margin-bottom: 24px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: #666;

        .meta-icon {
          font-size: 18px;
        }
      }
    }

    .tool-actions {
      display: flex;
      gap: 12px;

      .visit-btn {
        padding: 12px 32px;
        background: #1890ff;
        color: white;
        text-decoration: none;
        border-radius: 6px;
        font-weight: 500;
        transition: background 0.3s;

        &:hover {
          background: #40a9ff;
        }
      }

      .favorite-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 12px 24px;
        background: white;
        color: #1890ff;
        border: 2px solid #1890ff;
        border-radius: 6px;
        cursor: pointer;
        font-weight: 500;
        transition: all 0.3s;

        svg {
          fill: none;
          stroke: currentColor;
          stroke-width: 2;
          transition: all 0.3s;
        }

        &:hover {
          background: #f0f8ff;
        }

        &.favorited {
          background: #ff4d4f;
          border-color: #ff4d4f;
          color: white;

          svg {
            fill: white;
            stroke: none;
          }
        }
      }
    }
  }
}

.tool-description-section {
  margin-bottom: 24px;

  h2 {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 12px;
    color: #333;
  }

  .description-content {
    line-height: 1.7;
    color: #555;
    font-size: 14px;
  }
}

.tool-tags-section {
  margin-bottom: 24px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 10px;
    color: #333;
  }

  .tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .tag {
      padding: 6px 16px;
      background: #f5f5f5;
      border-radius: 16px;
      font-size: 14px;
      color: #666;
    }
  }
}

.reviews-section {
  .reviews-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #333;
    }

    .write-review-btn {
      padding: 10px 20px;
      background: #1890ff;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      transition: background 0.3s;

      &:hover {
        background: #40a9ff;
      }
    }
  }

  .review-form {
    background: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 24px;

    textarea {
      width: 100%;
      padding: 12px;
      border: 1px solid #e8e8e8;
      border-radius: 6px;
      font-size: 14px;
      resize: vertical;
      margin-bottom: 12px;
      font-family: inherit;

      &:focus {
        outline: none;
        border-color: #1890ff;
      }
    }

    .form-actions {
      display: flex;
      gap: 12px;

      button {
        padding: 10px 24px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 14px;
        transition: all 0.3s;
      }

      .submit-btn {
        background: #1890ff;
        color: white;

        &:hover {
          background: #40a9ff;
        }
      }

      .cancel-btn {
        background: white;
        color: #666;
        border: 1px solid #e8e8e8;

        &:hover {
          border-color: #1890ff;
          color: #1890ff;
        }
      }
    }
  }

  .reviews-list {
    .review-item {
      padding: 20px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .review-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;

          img {
            width: 40px;
            height: 40px;
            border-radius: 50%;
          }

          .username {
            font-weight: 500;
          }
        }
      }

      .review-content {
        line-height: 1.6;
        color: #333;
        margin-bottom: 8px;
      }

      .review-footer {
        .review-date {
          font-size: 13px;
          color: #999;
        }
      }
    }
  }

  .empty-reviews {
    text-align: center;
    padding: 60px 0;
    color: #999;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .detail-content {
    padding: 24px 16px;
  }

  .tool-header {
    flex-direction: column;
    align-items: center;
    text-align: center;

    .tool-main-info {
      width: 100%;

      .tool-meta {
        justify-content: center;
      }

      .tool-actions {
        justify-content: center;
      }
    }
  }
}
</style>
