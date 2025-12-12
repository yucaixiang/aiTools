<template>
  <div class="rating-stars">
    <div class="rating-display">
      <div class="stars">
        <span
          v-for="i in 5"
          :key="i"
          class="star"
          :class="{
            'filled': i <= (hoveredScore || userScore || 0),
            'interactive': interactive && isLoggedIn
          }"
          @click="handleClick(i)"
          @mouseenter="handleHover(i)"
          @mouseleave="handleHover(0)"
        >
          ★
        </span>
      </div>
      <div class="rating-info">
        <span class="average">{{ averageRating.toFixed(1) }}</span>
        <span class="count">({{ ratingCount }}人评分)</span>
      </div>
    </div>

    <div v-if="interactive && isLoggedIn && userScore" class="user-rating">
      你的评分: {{ userScore }}分
      <button @click="handleDelete" class="delete-btn">删除评分</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { submitRating, getToolRatingStats, deleteRating } from '@/api/rating'
import { useUserStore } from '@/stores/user'
import toast from '@/utils/toast'

const props = defineProps({
  toolId: {
    type: Number,
    required: true
  },
  interactive: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['rated', 'deleted'])

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const averageRating = ref(0)
const ratingCount = ref(0)
const userScore = ref(null)
const hoveredScore = ref(0)

// 加载评分统计
const loadRatingStats = async () => {
  try {
    const res = await getToolRatingStats(props.toolId)
    averageRating.value = res.data.averageRating || 0
    ratingCount.value = res.data.ratingCount || 0
    userScore.value = res.data.userScore || null
  } catch (error) {
    console.error('加载评分统计失败:', error)
  }
}

// 点击星星提交评分
const handleClick = async (score) => {
  if (!props.interactive || !isLoggedIn.value) return

  try {
    await submitRating({
      toolId: props.toolId,
      score
    })
    toast.success(userScore.value ? '评分已更新' : '评分成功')
    await loadRatingStats()
    emit('rated', score)
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 鼠标悬停
const handleHover = (score) => {
  if (!props.interactive || !isLoggedIn.value) return
  hoveredScore.value = score
}

// 删除评分
const handleDelete = async () => {
  if (!confirm('确定要删除评分吗？')) return

  try {
    await deleteRating(props.toolId)
    toast.success('评分已删除')
    await loadRatingStats()
    emit('deleted')
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  loadRatingStats()
})
</script>

<style scoped>
.rating-stars {
  padding: 20px 0;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stars {
  display: flex;
  gap: 4px;
}

.star {
  font-size: 24px;
  color: #ddd;
  transition: color 0.2s;
}

.star.filled {
  color: #ffb800;
}

.star.interactive {
  cursor: pointer;
}

.star.interactive:hover {
  color: #ffd700;
  transform: scale(1.1);
}

.rating-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.average {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.count {
  font-size: 14px;
  color: #666;
}

.user-rating {
  margin-top: 12px;
  font-size: 14px;
  color: #666;
}

.delete-btn {
  margin-left: 8px;
  padding: 2px 8px;
  font-size: 12px;
  color: #f56c6c;
  background: none;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  cursor: pointer;
}

.delete-btn:hover {
  background: #f56c6c;
  color: white;
}
</style>
