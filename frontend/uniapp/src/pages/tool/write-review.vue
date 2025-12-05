<template>
  <view class="container">
    <!-- Header -->
    <view class="header">
      <text class="back-btn" @click="handleBack">‹ 返回</text>
      <text class="header-title">{{ isEdit ? '编辑评论' : '写评论' }}</text>
      <text class="submit-btn" @click="handleSubmit">{{ isSubmitting ? '提交中...' : '提交' }}</text>
    </view>

    <!-- Tool Info -->
    <view v-if="toolInfo" class="tool-section">
      <image class="tool-logo" :src="toolInfo.logoUrl" mode="aspectFit" />
      <view class="tool-info">
        <text class="tool-name">{{ toolInfo.name }}</text>
        <text class="tool-tagline">{{ toolInfo.tagline }}</text>
      </view>
    </view>

    <!-- Rating Section -->
    <view class="section">
      <text class="section-title">
        <text class="required">*</text> 评分
      </text>
      <view class="rating-input">
        <view
          class="star-btn"
          v-for="n in 5"
          :key="n"
          @click="handleRatingChange(n)"
        >
          <text class="star-icon" :class="{ active: n <= formData.rating }">
            {{ n <= formData.rating ? '⭐' : '☆' }}
          </text>
        </view>
        <text class="rating-text">{{ getRatingText(formData.rating) }}</text>
      </view>
    </view>

    <!-- Content Section -->
    <view class="section">
      <text class="section-title">
        <text class="required">*</text> 评价内容
      </text>
      <textarea
        class="textarea-input"
        v-model="formData.content"
        placeholder="分享你的使用体验，帮助其他用户做出选择..."
        :maxlength="500"
        :auto-height="false"
      />
      <text class="char-count">{{ formData.content.length }}/500</text>
    </view>

    <!-- Pros Section -->
    <view class="section">
      <text class="section-title">优点（选填）</text>
      <view class="tags-input">
        <view
          class="tag-item"
          v-for="(pro, index) in formData.pros"
          :key="index"
        >
          <text class="tag-text">{{ pro }}</text>
          <text class="tag-remove" @click="removePro(index)">×</text>
        </view>
        <input
          v-if="showProInput"
          class="tag-input"
          v-model="proInput"
          placeholder="输入优点"
          @confirm="addPro"
          @blur="showProInput = false"
          :focus="showProInput"
        />
        <view v-else class="tag-add" @click="showProInput = true">
          <text>+ 添加优点</text>
        </view>
      </view>
      <view class="quick-tags">
        <text class="quick-tag-label">常用标签：</text>
        <view
          class="quick-tag"
          v-for="tag in proTags"
          :key="tag"
          @click="quickAddPro(tag)"
        >
          <text>{{ tag }}</text>
        </view>
      </view>
    </view>

    <!-- Cons Section -->
    <view class="section">
      <text class="section-title">缺点（选填）</text>
      <view class="tags-input">
        <view
          class="tag-item"
          v-for="(con, index) in formData.cons"
          :key="index"
        >
          <text class="tag-text">{{ con }}</text>
          <text class="tag-remove" @click="removeCon(index)">×</text>
        </view>
        <input
          v-if="showConInput"
          class="tag-input"
          v-model="conInput"
          placeholder="输入缺点"
          @confirm="addCon"
          @blur="showConInput = false"
          :focus="showConInput"
        />
        <view v-else class="tag-add" @click="showConInput = true">
          <text>+ 添加缺点</text>
        </view>
      </view>
      <view class="quick-tags">
        <text class="quick-tag-label">常用标签：</text>
        <view
          class="quick-tag"
          v-for="tag in conTags"
          :key="tag"
          @click="quickAddCon(tag)"
        >
          <text>{{ tag }}</text>
        </view>
      </view>
    </view>

    <!-- Submit Button -->
    <view class="submit-section">
      <button
        class="btn-submit"
        :disabled="!canSubmit || isSubmitting"
        @click="handleSubmit"
      >
        {{ isSubmitting ? '提交中...' : '发布评论' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getToolDetail } from '../../api/tool'
import { createReview, updateReview } from '../../api/review'

const toolInfo = ref<any>(null)
const formData = ref({
  rating: 0,
  content: '',
  pros: [] as string[],
  cons: [] as string[]
})

const toolId = ref<number>(0)
const reviewId = ref<number | null>(null)
const isEdit = ref(false)
const isSubmitting = ref(false)
const showProInput = ref(false)
const showConInput = ref(false)
const proInput = ref('')
const conInput = ref('')

const proTags = ['功能强大', '易于使用', '界面美观', '性价比高', '更新及时', '文档完善']
const conTags = ['价格较贵', '学习曲线陡', '功能单一', '性能一般', '客服响应慢', '缺少中文']

const canSubmit = computed(() => {
  return formData.value.rating > 0 && formData.value.content.trim().length >= 10
})

onLoad((options: any) => {
  if (options.toolId) {
    toolId.value = Number(options.toolId)
    loadToolInfo()
  }

  if (options.reviewId) {
    reviewId.value = Number(options.reviewId)
    isEdit.value = true
    // TODO: Load existing review data
  }
})

const loadToolInfo = async () => {
  try {
    const tool = await getToolDetail(toolId.value)
    toolInfo.value = tool
  } catch (error) {
    console.error('加载工具信息失败:', error)
  }
}

const handleRatingChange = (rating: number) => {
  formData.value.rating = rating
}

const getRatingText = (rating: number) => {
  const texts = ['', '很差', '较差', '一般', '推荐', '非常推荐']
  return texts[rating] || ''
}

const addPro = () => {
  const value = proInput.value.trim()
  if (value && !formData.value.pros.includes(value)) {
    formData.value.pros.push(value)
  }
  proInput.value = ''
  showProInput.value = false
}

const removePro = (index: number) => {
  formData.value.pros.splice(index, 1)
}

const quickAddPro = (tag: string) => {
  if (!formData.value.pros.includes(tag)) {
    formData.value.pros.push(tag)
  }
}

const addCon = () => {
  const value = conInput.value.trim()
  if (value && !formData.value.cons.includes(value)) {
    formData.value.cons.push(value)
  }
  conInput.value = ''
  showConInput.value = false
}

const removeCon = (index: number) => {
  formData.value.cons.splice(index, 1)
}

const quickAddCon = (tag: string) => {
  if (!formData.value.cons.includes(tag)) {
    formData.value.cons.push(tag)
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value || isSubmitting.value) return

  // Validate
  if (formData.value.rating === 0) {
    uni.showToast({
      title: '请选择评分',
      icon: 'none'
    })
    return
  }

  if (formData.value.content.trim().length < 10) {
    uni.showToast({
      title: '评价内容至少10个字',
      icon: 'none'
    })
    return
  }

  try {
    isSubmitting.value = true

    const data = {
      toolId: toolId.value,
      rating: formData.value.rating,
      content: formData.value.content.trim(),
      pros: formData.value.pros,
      cons: formData.value.cons
    }

    if (isEdit.value && reviewId.value) {
      await updateReview(reviewId.value, data)
      uni.showToast({
        title: '更新成功',
        icon: 'success'
      })
    } else {
      await createReview(data)
      uni.showToast({
        title: '发布成功',
        icon: 'success'
      })
    }

    // Go back
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error: any) {
    console.error('提交失败:', error)

    let errorMessage = '提交失败，请稍后重试'
    if (error.code === 3001) {
      errorMessage = '您已评论过该工具'
    } else if (error.message) {
      errorMessage = error.message
    }

    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  } finally {
    isSubmitting.value = false
  }
}

const handleBack = () => {
  if (formData.value.rating > 0 || formData.value.content.trim()) {
    uni.showModal({
      title: '确认',
      content: '确定要放弃本次编辑吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 100rpx;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 28rpx;
  color: #666;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.submit-btn {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
}

/* Tool Section */
.tool-section {
  display: flex;
  gap: 24rpx;
  padding: 30rpx 40rpx;
  background-color: #fff;
  margin-bottom: 20rpx;
}

.tool-logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

.tool-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.tool-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.tool-tagline {
  font-size: 24rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Section */
.section {
  background-color: #fff;
  padding: 30rpx 40rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 24rpx;
}

.required {
  color: #f44336;
  margin-right: 4rpx;
}

/* Rating Input */
.rating-input {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.star-btn {
  display: inline-block;
}

.star-icon {
  font-size: 48rpx;
  color: #ddd;
}

.star-icon.active {
  color: #ffc107;
}

.rating-text {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
  margin-left: 16rpx;
}

/* Textarea Input */
.textarea-input {
  width: 100%;
  min-height: 240rpx;
  padding: 24rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  box-sizing: border-box;
}

.char-count {
  display: block;
  text-align: right;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #999;
}

/* Tags Input */
.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 20rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  min-height: 80rpx;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background-color: #e8eaf6;
  color: #667eea;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.tag-text {
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-remove {
  font-size: 28rpx;
  font-weight: bold;
  cursor: pointer;
}

.tag-add {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 16rpx;
  background-color: #fff;
  border: 2rpx dashed #ccc;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #999;
}

.tag-input {
  flex: 1;
  min-width: 200rpx;
  padding: 8rpx 16rpx;
  background-color: #fff;
  border-radius: 20rpx;
  font-size: 24rpx;
  border: 2rpx solid #667eea;
}

/* Quick Tags */
.quick-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12rpx;
  margin-top: 20rpx;
}

.quick-tag-label {
  font-size: 24rpx;
  color: #999;
}

.quick-tag {
  padding: 6rpx 16rpx;
  background-color: #f5f7fa;
  color: #666;
  border-radius: 16rpx;
  font-size: 22rpx;
}

/* Submit Section */
.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 40rpx;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.btn-submit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
}

.btn-submit[disabled] {
  background: #e0e0e0;
  color: #999;
}
</style>
