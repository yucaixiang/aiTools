<template>
  <div class="home">
    <!-- Hero Section - 缩小版 -->
    <section class="hero">
      <div class="container">
        <h1 class="hero-title">用AI寻找最好的工具和软件</h1>
        <p class="hero-subtitle">让AI帮你发现最适合的工具，提升你的工作效率</p>
      </div>
    </section>

    <!-- Main Content -->
    <div class="container">
      <!-- 搜索和分类筛选 -->
      <section class="categories-section">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索工具名称、标语、描述..."
            @keyup.enter="handleSearch"
          />
          <button @click="handleSearch" class="search-btn">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"/>
            </svg>
          </button>
        </div>

        <!-- 分类筛选 -->
        <div class="categories-grid">
          <button
            v-for="cat in categories"
            :key="cat.id"
            :class="['category-chip', { active: selectedCategory === cat.id }]"
            @click="selectedCategory = cat.id === selectedCategory ? null : cat.id"
          >
            <span class="category-icon">{{ cat.icon }}</span>
            <span>{{ cat.name }}</span>
          </button>
        </div>
      </section>

      <!-- 工具列表 -->
      <section class="tools-section">
        <div class="section-header">
          <h2>{{ selectedCategory ? '筛选结果' : '热门工具' }}</h2>
          <div class="sort-options">
            <button
              v-for="option in sortOptions"
              :key="option.value"
              :class="['sort-btn', { active: sortBy === option.value }]"
              @click="sortBy = option.value"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div v-if="loading && tools.length === 0" class="loading">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <div v-else-if="tools.length === 0" class="empty">
          <p>暂无工具</p>
        </div>

        <div v-else class="tools-grid">
          <div
            v-for="tool in tools"
            :key="tool.id"
            class="tool-card"
            @click="goToDetail(tool.id)"
          >
            <div class="tool-header">
              <img :src="tool.logoUrl || '/default-logo.png'" alt="logo" class="tool-logo" />
              <div class="tool-info">
                <h3 class="tool-name">{{ tool.name }}</h3>
                <p class="tool-tagline">{{ tool.tagline }}</p>
              </div>
            </div>

            <p class="tool-description">{{ tool.description }}</p>

            <div class="tool-tags">
              <span v-for="tag in tool.tags?.slice(0, 3)" :key="tag.id" class="tag">
                {{ tag.name }}
              </span>
            </div>

            <div class="tool-footer">
              <div class="tool-stat">
                <span class="stat-icon">⭐</span>
                <span>{{ tool.averageRating || '暂无评分' }}</span>
              </div>
              <div class="tool-stat">
                <span class="stat-icon">❤️</span>
                <span>{{ tool.favoriteCount || 0 }}</span>
              </div>
              <div class="tool-stat">
                <span class="stat-icon">👁️</span>
                <span>{{ tool.viewCount || 0 }}</span>
              </div>
              <div class="tool-stat">
                <span class="stat-icon">💬</span>
                <span>{{ tool.reviewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore && !loading" class="load-more">
          <button @click="loadMore" class="load-more-btn">加载更多</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTools } from '@/api/tool'

const router = useRouter()

const searchKeyword = ref('')
const selectedCategory = ref(null)
const sortBy = ref('hot')
const loading = ref(false)
const tools = ref([])
const page = ref(1)
const hasMore = ref(true)

const categories = ref([
  { id: null, name: '全部', icon: '🌟' },
  { id: 1, name: 'AI写作', icon: '✍️' },
  { id: 2, name: 'AI绘画', icon: '🎨' },
  { id: 3, name: '代码助手', icon: '💻' },
  { id: 4, name: '数据分析', icon: '📊' },
  { id: 5, name: '办公效率', icon: '📝' },
  { id: 6, name: '设计工具', icon: '🎭' }
])

const sortOptions = [
  { value: 'hot', label: '最热门' },
  { value: 'latest', label: '最新' },
  { value: 'rating', label: '评分' }
]

// 监听分类和排序变化，重新加载数据
watch([selectedCategory, sortBy], () => {
  resetAndLoad()
})

onMounted(async () => {
  await loadTools()
})

// 重置并加载数据
async function resetAndLoad() {
  tools.value = []
  page.value = 1
  hasMore.value = true
  await loadTools()
}

async function loadTools() {
  loading.value = true
  try {
    // 构建符合后端ToolQueryDTO的请求体
    const queryDTO = {
      current: page.value,
      size: 12
    }

    // 添加搜索关键词
    if (searchKeyword.value && searchKeyword.value.trim()) {
      queryDTO.keyword = searchKeyword.value.trim()
    }

    // 添加分类筛选参数
    if (selectedCategory.value) {
      queryDTO.categoryId = selectedCategory.value
    }

    // 添加排序参数 - 使用后端定义的值
    if (sortBy.value === 'latest') {
      queryDTO.sortBy = 'launch'  // 后端使用 launch 表示发布时间
      queryDTO.sortOrder = 'desc'
    } else if (sortBy.value === 'rating') {
      queryDTO.sortBy = 'rating'
      queryDTO.sortOrder = 'desc'
    } else {
      queryDTO.sortBy = 'upvote'  // 后端使用 upvote 表示点赞数
      queryDTO.sortOrder = 'desc'
    }

    const res = await getTools(queryDTO)

    if (res.data && res.data.records) {
      if (page.value === 1) {
        tools.value = res.data.records
      } else {
        tools.value = [...tools.value, ...res.data.records]
      }
      hasMore.value = res.data.records.length === 12
    }
  } catch (error) {
    console.error('加载工具失败:', error)
    // 如果API失败，显示错误提示
    alert('加载工具失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  page.value++
  await loadTools()
}

function handleSearch() {
  // 执行搜索（包括空搜索清除关键词）
  resetAndLoad()
}

function goToDetail(id) {
  router.push(`/tool/${id}`)
}
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
}

.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;  // 从80px缩小到40px
  color: white;
  text-align: center;

  .container {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .hero-title {
    font-size: 32px;  // 从48px缩小到32px
    font-weight: 700;
    margin-bottom: 12px;  // 从16px缩小到12px
  }

  .hero-subtitle {
    font-size: 16px;  // 从20px缩小到16px
    opacity: 0.95;
    margin-bottom: 0;  // 移除底部边距
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.categories-section {
  padding: 30px 0 20px;

  .search-box {
    max-width: 600px;
    margin: 0 auto 24px;
    display: flex;
    background: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid #e8e8e8;

    input {
      flex: 1;
      border: none;
      padding: 12px 16px;
      font-size: 14px;
      outline: none;
    }

    .search-btn {
      background: #1890ff;
      border: none;
      padding: 0 20px;
      color: white;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #40a9ff;
      }
    }
  }

  .categories-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .category-chip {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: white;
    border: 2px solid #e8e8e8;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;

    &:hover {
      border-color: #1890ff;
      color: #1890ff;
    }

    &.active {
      background: #1890ff;
      border-color: #1890ff;
      color: white;
    }

    .category-icon {
      font-size: 18px;
    }
  }
}

.tools-section {
  padding: 20px 0 60px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      font-weight: 600;
    }

    .sort-options {
      display: flex;
      gap: 8px;
    }

    .sort-btn {
      padding: 6px 16px;
      background: white;
      border: 1px solid #e8e8e8;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;

      &:hover {
        border-color: #1890ff;
        color: #1890ff;
      }

      &.active {
        background: #1890ff;
        border-color: #1890ff;
        color: white;
      }
    }
  }

  .loading {
    text-align: center;
    padding: 60px 0;

    .spinner {
      width: 40px;
      height: 40px;
      border: 4px solid #f0f0f0;
      border-top-color: #1890ff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 16px;
    }
  }

  .empty {
    text-align: center;
    padding: 60px 0;
    color: #999;
  }

  .tools-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
  }

  .tool-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid #f0f0f0;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }

    .tool-header {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
    }

    .tool-logo {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      object-fit: cover;
      flex-shrink: 0;
    }

    .tool-info {
      flex: 1;
      min-width: 0;
    }

    .tool-name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 4px;
    }

    .tool-tagline {
      font-size: 14px;
      color: #666;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .tool-description {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin-bottom: 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .tool-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 16px;
    }

    .tag {
      padding: 4px 12px;
      background: #f5f5f5;
      border-radius: 12px;
      font-size: 12px;
      color: #666;
    }

    .tool-footer {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
      font-size: 13px;
      color: #999;

      .tool-stat {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;

        .stat-icon {
          font-size: 14px;
        }
      }
    }
  }

  .load-more {
    text-align: center;
    padding-top: 40px;

    .load-more-btn {
      padding: 12px 32px;
      background: white;
      border: 2px solid #e8e8e8;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 15px;

      &:hover {
        border-color: #1890ff;
        color: #1890ff;
      }
    }
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .hero {
    padding: 60px 0;

    .hero-title {
      font-size: 32px;
    }

    .hero-subtitle {
      font-size: 16px;
    }
  }

  .tools-section {
    .section-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }

    .tools-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
