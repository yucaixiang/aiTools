<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>工具提交趋势</span>
          </template>
          <div ref="toolsTrendRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户注册趋势</span>
          </template>
          <div ref="usersTrendRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>分类分布</span>
          </template>
          <div ref="categoryDistRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>工具状态分布</span>
          </template>
          <div ref="statusDistRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>热门工具Top 10</span>
          </template>
          <div ref="topToolsRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>活跃用户Top 10</span>
          </template>
          <el-table :data="topUsers" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="danger">{{ $index + 1 }}</el-tag>
                <el-tag v-else-if="$index === 1" type="warning">{{ $index + 1 }}</el-tag>
                <el-tag v-else-if="$index === 2" type="success">{{ $index + 1 }}</el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="toolsCount" label="提交工具数" />
            <el-table-column prop="reviewsCount" label="评论数" />
            <el-table-column prop="favoritesCount" label="收藏数" />
            <el-table-column prop="totalScore" label="活跃度" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const toolsTrendRef = ref(null)
const usersTrendRef = ref(null)
const categoryDistRef = ref(null)
const statusDistRef = ref(null)
const topToolsRef = ref(null)
const topUsers = ref([])

// 模拟数据 - 实际应该从API获取
const mockToolsTrendData = () => {
  const dates = []
  const values = []
  for (let i = 29; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
    values.push(Math.floor(Math.random() * 20) + 5)
  }
  return { dates, values }
}

const mockUsersTrendData = () => {
  const dates = []
  const values = []
  for (let i = 29; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
    values.push(Math.floor(Math.random() * 30) + 10)
  }
  return { dates, values }
}

const renderToolsTrend = () => {
  if (!toolsTrendRef.value) return

  const chart = echarts.init(toolsTrendRef.value)
  const data = mockToolsTrendData()

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '工具数',
        type: 'line',
        smooth: true,
        data: data.values,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
          ])
        },
        itemStyle: {
          color: '#667eea'
        }
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const renderUsersTrend = () => {
  if (!usersTrendRef.value) return

  const chart = echarts.init(usersTrendRef.value)
  const data = mockUsersTrendData()

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '用户数',
        type: 'line',
        smooth: true,
        data: data.values,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(66, 230, 149, 0.5)' },
            { offset: 1, color: 'rgba(66, 230, 149, 0.1)' }
          ])
        },
        itemStyle: {
          color: '#42e695'
        }
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const renderCategoryDist = () => {
  if (!categoryDistRef.value) return

  const chart = echarts.init(categoryDistRef.value)

  const option = {
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
        radius: '60%',
        data: [
          { value: 45, name: 'AI写作' },
          { value: 38, name: 'AI绘画' },
          { value: 32, name: '代码助手' },
          { value: 28, name: '数据分析' },
          { value: 25, name: '办公效率' },
          { value: 22, name: '设计工具' },
          { value: 18, name: '其他' }
        ],
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
  window.addEventListener('resize', () => chart.resize())
}

const renderStatusDist = () => {
  if (!statusDistRef.value) return

  const chart = echarts.init(statusDistRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside'
        },
        labelLine: {
          show: true
        },
        data: [
          { value: 165, name: '已通过', itemStyle: { color: '#67c23a' } },
          { value: 23, name: '待审核', itemStyle: { color: '#e6a23c' } },
          { value: 12, name: '已拒绝', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const renderTopTools = () => {
  if (!topToolsRef.value) return

  const chart = echarts.init(topToolsRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: ['工具J', '工具I', '工具H', '工具G', '工具F', '工具E', '工具D', '工具C', '工具B', '工具A']
    },
    series: [
      {
        name: '浏览量',
        type: 'bar',
        data: [1520, 1820, 2010, 2340, 2680, 2890, 3120, 3450, 3780, 4200],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const loadStatistics = async () => {
  try {
    // TODO: 调用API获取统计数据
    // const res = await getStatisticsData()

    // 模拟活跃用户数据
    topUsers.value = [
      { username: 'user001', toolsCount: 15, reviewsCount: 45, favoritesCount: 32, totalScore: 920 },
      { username: 'user002', toolsCount: 12, reviewsCount: 38, favoritesCount: 28, totalScore: 780 },
      { username: 'user003', toolsCount: 10, reviewsCount: 35, favoritesCount: 25, totalScore: 700 },
      { username: 'user004', toolsCount: 9, reviewsCount: 30, favoritesCount: 22, totalScore: 610 },
      { username: 'user005', toolsCount: 8, reviewsCount: 28, favoritesCount: 20, totalScore: 560 },
      { username: 'user006', toolsCount: 7, reviewsCount: 25, favoritesCount: 18, totalScore: 500 },
      { username: 'user007', toolsCount: 6, reviewsCount: 22, favoritesCount: 15, totalScore: 430 },
      { username: 'user008', toolsCount: 5, reviewsCount: 20, favoritesCount: 12, totalScore: 370 },
      { username: 'user009', toolsCount: 4, reviewsCount: 18, favoritesCount: 10, totalScore: 320 },
      { username: 'user010', toolsCount: 3, reviewsCount: 15, favoritesCount: 8, totalScore: 260 }
    ]

    await nextTick()
    renderToolsTrend()
    renderUsersTrend()
    renderCategoryDist()
    renderStatusDist()
    renderTopTools()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error(error)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.statistics {
  // 样式已通过全局样式定义
}
</style>
