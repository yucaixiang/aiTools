<template>
  <view class="container">
    <!-- Chat Header -->
    <view class="chat-header">
      <text class="header-title">AI工具推荐助手</text>
      <view class="header-actions">
        <text class="action-btn" @click="showHistoryPopup = true">📝</text>
        <text class="action-btn" @click="handleNewChat">➕</text>
      </view>
    </view>

    <!-- Messages List -->
    <scroll-view
      class="messages-container"
      scroll-y
      :scroll-into-view="scrollToView"
      scroll-with-animation
    >
      <view class="messages-list">
        <!-- Welcome Message -->
        <view v-if="messages.length === 0" class="welcome">
          <text class="welcome-title">👋 你好！我是AI工具推荐助手</text>
          <text class="welcome-subtitle">告诉我你的需求，我会为你推荐合适的工具</text>

          <view class="quick-questions">
            <text class="quick-title">快速开始：</text>
            <view
              class="quick-item"
              v-for="(question, index) in quickQuestions"
              :key="index"
              @click="handleQuickQuestion(question)"
            >
              <text class="quick-text">{{ question }}</text>
            </view>
          </view>
        </view>

        <!-- Message Items -->
        <view
          v-for="(message, index) in messages"
          :key="message.id"
          :id="'msg-' + index"
          class="message-wrapper"
          :class="message.role === 'user' ? 'message-user' : 'message-assistant'"
        >
          <view class="message-bubble">
            <view v-if="message.role === 'assistant'" class="assistant-avatar">
              <text class="avatar-icon">🤖</text>
            </view>

            <view class="message-content">
              <text class="message-text">{{ message.content }}</text>

              <!-- Tool Recommendations -->
              <view v-if="message.tools && message.tools.length > 0" class="tool-recommendations">
                <text class="recommendations-title">为你推荐以下工具：</text>
                <view
                  class="tool-card"
                  v-for="tool in message.tools"
                  :key="tool.id"
                  @click="handleToolClick(tool.id)"
                >
                  <image class="tool-logo" :src="tool.logoUrl" mode="aspectFit" />
                  <view class="tool-info">
                    <text class="tool-name">{{ tool.name }}</text>
                    <text class="tool-tagline">{{ tool.tagline }}</text>
                    <view class="tool-meta">
                      <view class="meta-item">
                        <text class="meta-icon">⭐</text>
                        <text class="meta-value">{{ tool.averageRating }}</text>
                      </view>
                      <view class="meta-item">
                        <text class="pricing-badge" :class="getPricingClass(tool.pricingModel)">
                          {{ getPricingText(tool.pricingModel) }}
                        </text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>

              <text class="message-time">{{ formatTime(message.timestamp) }}</text>
            </view>

            <view v-if="message.role === 'user'" class="user-avatar">
              <text class="avatar-icon">👤</text>
            </view>
          </view>
        </view>

        <!-- Typing Indicator -->
        <view v-if="isTyping" class="message-wrapper message-assistant">
          <view class="message-bubble">
            <view class="assistant-avatar">
              <text class="avatar-icon">🤖</text>
            </view>
            <view class="typing-indicator">
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Input Area -->
    <view class="input-area">
      <textarea
        class="input-field"
        v-model="inputMessage"
        placeholder="描述你需要的工具..."
        :auto-height="true"
        :maxlength="500"
        @confirm="handleSend"
      />
      <button
        class="send-btn"
        :disabled="!inputMessage.trim() || isTyping"
        @click="handleSend"
      >
        {{ isTyping ? '发送中...' : '发送' }}
      </button>
    </view>

    <!-- History Popup -->
    <view v-if="showHistoryPopup" class="popup-overlay" @click="showHistoryPopup = false">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">对话历史</text>
          <text class="popup-close" @click="showHistoryPopup = false">×</text>
        </view>

        <scroll-view class="popup-body" scroll-y>
          <view v-if="chatSessions.length === 0" class="empty-history">
            <text>暂无对话历史</text>
          </view>

          <view
            v-for="session in chatSessions"
            :key="session.id"
            class="session-item"
            @click="handleLoadSession(session)"
          >
            <view class="session-info">
              <text class="session-title">{{ session.title || '未命名对话' }}</text>
              <text class="session-time">{{ formatDate(session.createdAt) }}</text>
            </view>
            <text class="session-delete" @click.stop="handleDeleteSession(session.id)">🗑️</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { sendChat, getChatHistory, createSession } from '../../api/ai'
import type { Tool } from '../../api/tool'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  tools?: Tool[]
  timestamp: Date
}

interface ChatSession {
  id: number
  title: string
  createdAt: string
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isTyping = ref(false)
const scrollToView = ref('')
const showHistoryPopup = ref(false)
const chatSessions = ref<ChatSession[]>([])
const currentSessionId = ref<number | null>(null)

const quickQuestions = [
  '我想找一个AI写作助手',
  '有什么好用的设计工具推荐吗？',
  '推荐一些免费的AI绘画工具',
  '适合程序员用的AI编程助手'
]

onLoad(() => {
  loadChatSessions()
})

const loadChatSessions = () => {
  // TODO: Load from API or local storage
  const storedSessions = uni.getStorageSync('chatSessions') || []
  chatSessions.value = storedSessions
}

const handleQuickQuestion = (question: string) => {
  inputMessage.value = question
  handleSend()
}

const handleSend = async () => {
  const message = inputMessage.value.trim()
  if (!message || isTyping.value) return

  // Add user message
  const userMessage: Message = {
    id: Date.now().toString(),
    role: 'user',
    content: message,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  inputMessage.value = ''

  // Scroll to bottom
  await nextTick()
  scrollToView.value = `msg-${messages.value.length - 1}`

  // Show typing indicator
  isTyping.value = true

  try {
    // Build conversation history
    const history = messages.value.map(msg => ({
      role: msg.role,
      content: msg.content
    }))

    // Send to AI service
    const response = await sendChat({
      message: message,
      sessionId: currentSessionId.value,
      history: history.slice(0, -1) // Exclude current message
    })

    // Add assistant response
    const assistantMessage: Message = {
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: response.reply,
      tools: response.recommendedTools || [],
      timestamp: new Date()
    }
    messages.value.push(assistantMessage)

    // Update session ID
    if (response.sessionId && !currentSessionId.value) {
      currentSessionId.value = response.sessionId
      saveCurrentSession()
    }

    // Scroll to bottom
    await nextTick()
    scrollToView.value = `msg-${messages.value.length - 1}`
  } catch (error: any) {
    console.error('发送消息失败:', error)

    if (error.code === 401) {
      uni.showModal({
        title: '提示',
        content: '请先登录',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/auth/login'
            })
          }
        }
      })
    } else {
      uni.showToast({
        title: error.message || '发送失败',
        icon: 'none'
      })
    }
  } finally {
    isTyping.value = false
  }
}

const handleNewChat = () => {
  if (messages.value.length > 0) {
    uni.showModal({
      title: '确认',
      content: '确定要开始新对话吗？当前对话将被保存',
      success: (res) => {
        if (res.confirm) {
          saveCurrentSession()
          messages.value = []
          currentSessionId.value = null
        }
      }
    })
  }
}

const handleToolClick = (toolId: number) => {
  uni.navigateTo({
    url: `/pages/tool/detail?id=${toolId}`
  })
}

const handleLoadSession = async (session: ChatSession) => {
  try {
    showHistoryPopup.value = false

    // Save current session first
    if (messages.value.length > 0 && currentSessionId.value) {
      saveCurrentSession()
    }

    // Load session history
    const history = await getChatHistory(session.id)

    // Convert to messages
    messages.value = history.map((item: any) => ({
      id: item.id.toString(),
      role: item.role,
      content: item.content,
      tools: item.recommendedTools,
      timestamp: new Date(item.createdAt)
    }))

    currentSessionId.value = session.id

    // Scroll to bottom
    await nextTick()
    scrollToView.value = `msg-${messages.value.length - 1}`
  } catch (error) {
    console.error('加载对话失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const handleDeleteSession = (sessionId: number) => {
  uni.showModal({
    title: '确认',
    content: '确定要删除这个对话吗？',
    success: (res) => {
      if (res.confirm) {
        chatSessions.value = chatSessions.value.filter(s => s.id !== sessionId)
        uni.setStorageSync('chatSessions', chatSessions.value)

        if (currentSessionId.value === sessionId) {
          messages.value = []
          currentSessionId.value = null
        }

        uni.showToast({
          title: '已删除',
          icon: 'success'
        })
      }
    }
  })
}

const saveCurrentSession = () => {
  if (messages.value.length === 0) return

  const session: ChatSession = {
    id: currentSessionId.value || Date.now(),
    title: messages.value[0]?.content.slice(0, 20) + '...',
    createdAt: new Date().toISOString()
  }

  const existingIndex = chatSessions.value.findIndex(s => s.id === session.id)
  if (existingIndex >= 0) {
    chatSessions.value[existingIndex] = session
  } else {
    chatSessions.value.unshift(session)
  }

  uni.setStorageSync('chatSessions', chatSessions.value)
}

const getPricingClass = (model: string) => {
  const classMap: Record<string, string> = {
    'FREE': 'pricing-free',
    'FREEMIUM': 'pricing-freemium',
    'PAID': 'pricing-paid',
    'OPEN_SOURCE': 'pricing-opensource'
  }
  return classMap[model] || ''
}

const getPricingText = (model: string) => {
  const textMap: Record<string, string> = {
    'FREE': '免费',
    'FREEMIUM': '免费增值',
    'PAID': '付费',
    'OPEN_SOURCE': '开源'
  }
  return textMap[model] || model
}

const formatTime = (date: Date) => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`

  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

/* Header */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.header-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  font-size: 32rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 50%;
}

/* Messages Container */
.messages-container {
  flex: 1;
  padding: 40rpx;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* Welcome */
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  text-align: center;
}

.welcome-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 16rpx;
}

.welcome-subtitle {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 60rpx;
}

.quick-questions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.quick-title {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.quick-item {
  padding: 24rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  text-align: left;
}

/* Messages */
.message-wrapper {
  display: flex;
}

.message-user {
  justify-content: flex-end;
}

.message-assistant {
  justify-content: flex-start;
}

.message-bubble {
  display: flex;
  gap: 20rpx;
  max-width: 80%;
}

.assistant-avatar,
.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 32rpx;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.message-user .message-content {
  align-items: flex-end;
}

.message-text {
  padding: 24rpx 28rpx;
  background-color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  word-wrap: break-word;
}

.message-user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.message-time {
  font-size: 22rpx;
  color: #999;
  padding: 0 8rpx;
}

/* Tool Recommendations */
.tool-recommendations {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 16rpx;
}

.recommendations-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #666;
  padding: 0 8rpx;
}

.tool-card {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  background-color: #f5f7fa;
  border-radius: 16rpx;
}

.tool-logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  background-color: #fff;
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
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 4rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #999;
}

.meta-icon {
  font-size: 20rpx;
}

.meta-value {
  font-weight: 500;
  color: #666;
}

.pricing-badge {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.pricing-free {
  background-color: #e8f5e9;
  color: #4caf50;
}

.pricing-freemium {
  background-color: #e3f2fd;
  color: #2196f3;
}

.pricing-paid {
  background-color: #fff3e0;
  color: #ff9800;
}

.pricing-opensource {
  background-color: #f3e5f5;
  color: #9c27b0;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  gap: 8rpx;
  padding: 24rpx 28rpx;
  background-color: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.typing-dot {
  width: 12rpx;
  height: 12rpx;
  background-color: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10rpx);
    opacity: 1;
  }
}

/* Input Area */
.input-area {
  display: flex;
  gap: 20rpx;
  padding: 30rpx 40rpx;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
}

.input-field {
  flex: 1;
  min-height: 80rpx;
  max-height: 200rpx;
  padding: 20rpx 24rpx;
  background-color: #f5f7fa;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.send-btn {
  height: 80rpx;
  padding: 0 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  border: none;
  flex-shrink: 0;
}

.send-btn[disabled] {
  background: #e0e0e0;
  color: #999;
}

/* History Popup */
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.popup-content {
  width: 100%;
  max-height: 70vh;
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}

.popup-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.popup-body {
  flex: 1;
  padding: 20rpx 40rpx;
}

.empty-history {
  text-align: center;
  padding: 80rpx 0;
  font-size: 28rpx;
  color: #999;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1px solid #f5f5f5;
}

.session-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.session-title {
  font-size: 28rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 24rpx;
  color: #999;
}

.session-delete {
  font-size: 32rpx;
  padding: 8rpx;
}
</style>
