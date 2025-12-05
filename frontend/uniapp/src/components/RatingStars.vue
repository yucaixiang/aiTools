<template>
  <view class="rating-stars" :class="{ interactive: interactive }">
    <view
      v-for="n in maxStars"
      :key="n"
      class="star-item"
      @click="handleClick(n)"
    >
      <text class="star-icon" :class="getStarClass(n)">
        {{ getStarSymbol(n) }}
      </text>
    </view>
    <text v-if="showValue" class="rating-value">{{ value }}</text>
  </view>
</template>

<script setup lang="ts">
interface Props {
  value: number
  maxStars?: number
  size?: 'small' | 'medium' | 'large'
  interactive?: boolean
  showValue?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  maxStars: 5,
  size: 'medium',
  interactive: false,
  showValue: false
})

const emit = defineEmits<{
  change: [value: number]
}>()

const handleClick = (star: number) => {
  if (props.interactive) {
    emit('change', star)
  }
}

const getStarClass = (star: number) => {
  if (props.value >= star) {
    return 'star-full'
  } else if (props.value >= star - 0.5) {
    return 'star-half'
  } else {
    return 'star-empty'
  }
}

const getStarSymbol = (star: number) => {
  if (props.value >= star) {
    return '⭐'
  } else if (props.value >= star - 0.5) {
    return '⭐'
  } else {
    return '☆'
  }
}
</script>

<style scoped>
.rating-stars {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
}

.rating-stars.interactive .star-item {
  cursor: pointer;
}

.star-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.star-icon {
  font-size: 28rpx;
  transition: transform 0.2s;
}

.rating-stars.interactive .star-item:active .star-icon {
  transform: scale(1.2);
}

.star-full {
  color: #ffc107;
}

.star-half {
  color: #ffc107;
  opacity: 0.5;
}

.star-empty {
  color: #ddd;
}

.rating-value {
  margin-left: 12rpx;
  font-size: 24rpx;
  font-weight: 500;
  color: #666;
}

/* Size variants */
.rating-stars.size-small .star-icon {
  font-size: 20rpx;
}

.rating-stars.size-medium .star-icon {
  font-size: 28rpx;
}

.rating-stars.size-large .star-icon {
  font-size: 36rpx;
}
</style>
