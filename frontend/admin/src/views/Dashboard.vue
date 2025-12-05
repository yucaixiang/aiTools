<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card blue" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">工具总数</div>
              <div class="card-value">{{ statistics.totalTools }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card green" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">用户总数</div>
              <div class="card-value">{{ statistics.totalUsers }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card orange" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon><ChatLineSquare /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">评论总数</div>
              <div class="card-value">{{ statistics.totalReviews }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card purple" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">待审核工具</div>
              <div class="card-value">{{ statistics.pendingTools }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近提交的工具</span>
              <el-button type="primary" link @click="$router.push('/tools')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTools" style="width: 100%">
            <el-table-column prop="name" label="工具名称" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'approved'" type="success">已通过</el-tag>
                <el-tag v-else-if="row.status === 'pending'" type="warning">待审核</el-tag>
                <el-tag v-else type="danger">已拒绝</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新评论</span>
              <el-button type="primary" link @click="$router.push('/reviews')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentReviews" style="width: 100%">
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="toolName" label="工具名称" />
            <el-table-column prop="rating" label="评分" width="100">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="评论时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>分类统计</span>
          </template>
          <div ref="chartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Tools, User, ChatLineSquare, Warning } from '@element-plus/icons-vue'
import { getStatistics } from '@/api/admin'
import * as echarts from 'echarts'

const statistics = ref({
  totalTools: 0,
  totalUsers: 0,
  totalReviews: 0,
  pendingTools: 0
})

const recentTools = ref([])
const recentReviews = ref([])
const chartRef = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data.overview || {
      totalTools: 0,
      totalUsers: 0,
      totalReviews: 0,
      pendingTools: 0
    }
    recentTools.value = res.data.recentTools || []
    recentReviews.value = res.data.recentReviews || []

    // 渲染图表
    await nextTick()
    renderChart(res.data.categoryStats || [])
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const renderChart = (categoryStats) => {
  if (!chartRef.value) return

  const chart = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '各分类工具数量',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: categoryStats.map(item => ({
          name: item.categoryName,
          value: item.count
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)

  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stats-row {
    .stats-card {
      :deep(.el-card__body) {
        padding: 20px;
      }

      &.blue {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border: none;
      }

      &.green {
        background: linear-gradient(135deg, #42e695 0%, #3bb2b8 100%);
        color: white;
        border: none;
      }

      &.orange {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: white;
        border: none;
      }

      &.purple {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
        color: #333;
        border: none;
      }

      .card-content {
        display: flex;
        align-items: center;
        gap: 20px;

        .card-icon {
          font-size: 48px;
          opacity: 0.8;
        }

        .card-info {
          flex: 1;

          .card-title {
            font-size: 14px;
            margin-bottom: 8px;
            opacity: 0.9;
          }

          .card-value {
            font-size: 32px;
            font-weight: bold;
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
