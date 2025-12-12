<template>
  <div class="chat-page">
    <div class="container">
      <div class="chat-container">
        <div class="chat-header">
          <h2>🤖 AI工具推荐助手</h2>
          <p>告诉我你的需求，我会为你推荐合适的AI工具</p>
        </div>

        <div class="messages-container" ref="messagesContainer">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.role]"
          >
            <div class="message-content">
              <div v-if="message.role === 'assistant'" class="avatar">🤖</div>
              <div v-else class="avatar">👤</div>
              <div class="content">
                <div v-if="message.role === 'assistant' && message.recommendations" class="recommendations">
                  <p class="message-text">{{ message.content }}</p>
                  <div class="tools-list">
                    <div
                      v-for="tool in message.recommendations"
                      :key="tool.id"
                      class="tool-card"
                      @click="goToTool(tool.id)"
                    >
                      <img :src="tool.logoUrl || '/default-logo.png'" alt="logo" />
                      <div class="tool-info">
                        <h4>{{ tool.name }}</h4>
                        <p>{{ tool.tagline }}</p>
                      </div>
                    </div>
                  </div>
                </div>
                <p v-else class="message-text">{{ message.content }}</p>
              </div>
            </div>
          </div>

          <div v-if="loading" class="message assistant">
            <div class="message-content">
              <div class="avatar">🤖</div>
              <div class="content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="input-area">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="描述你需要的工具，比如：我需要一个AI写作工具..."
          />
          <button @click="sendMessage" :disabled="!userInput.trim() || loading">
            发送
          </button>
        </div>

        <div class="quick-questions">
          <p>试试问我：</p>
          <button
            v-for="(q, i) in quickQuestions"
            :key="i"
            @click="askQuestion(q)"
            class="quick-q"
          >
            {{ q }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { chat } from '@/api/ai'

const router = useRouter()

const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是AI工具推荐助手。请告诉我你的需求，我会为你推荐最合适的AI工具。'
  }
])
const userInput = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

const quickQuestions = [
  '我需要一个AI写作工具',
  '有什么好用的AI绘画工具？',
  '推荐一些编程辅助工具',
  '帮我找数据分析工具'
]

async function sendMessage() {
  if (!userInput.value.trim() || loading.value) return

  const message = userInput.value.trim()
  messages.value.push({
    role: 'user',
    content: message
  })

  userInput.value = ''
  loading.value = true

  await scrollToBottom()

  try {
    const res = await chat({ message })
    messages.value.push({
      role: 'assistant',
      content: res.data.reply || '抱歉，我暂时无法理解您的需求。',
      recommendations: res.data.recommendations
    })
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

function askQuestion(question) {
  userInput.value = question
  sendMessage()
}

function goToTool(id) {
  router.push(`/tool/${id}`)
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
</script>

<style lang="scss" scoped>
.chat-page {
  padding: 40px 0;
  min-height: calc(100vh - 128px);
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.chat-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
  min-height: 600px;
}

.chat-header {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;

  h2 {
    margin-bottom: 8px;
    font-size: 24px;
  }

  p {
    opacity: 0.9;
    font-size: 14px;
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  .message {
    display: flex;

    &.user {
      justify-content: flex-end;

      .message-content {
        flex-direction: row-reverse;
      }

      .content {
        background: #1890ff;
        color: white;
      }
    }

    &.assistant {
      .content {
        background: #f5f5f5;
      }
    }

    .message-content {
      display: flex;
      gap: 12px;
      max-width: 80%;

      .avatar {
        width: 36px;
        height: 36px;
        background: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        flex-shrink: 0;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .content {
        padding: 12px 16px;
        border-radius: 12px;
        line-height: 1.6;
        font-size: 15px;

        .message-text {
          margin: 0;
        }

        .recommendations {
          .tools-list {
            margin-top: 16px;
            display: flex;
            flex-direction: column;
            gap: 12px;
          }

          .tool-card {
            display: flex;
            gap: 12px;
            padding: 12px;
            background: white;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              transform: translateX(4px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }

            img {
              width: 48px;
              height: 48px;
              border-radius: 8px;
              object-fit: cover;
            }

            .tool-info {
              flex: 1;

              h4 {
                margin: 0 0 4px;
                font-size: 15px;
                color: #333;
              }

              p {
                margin: 0;
                font-size: 13px;
                color: #666;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }
        }

        .typing-indicator {
          display: flex;
          gap: 4px;

          span {
            width: 8px;
            height: 8px;
            background: #999;
            border-radius: 50%;
            animation: typing 1.4s infinite;

            &:nth-child(2) {
              animation-delay: 0.2s;
            }

            &:nth-child(3) {
              animation-delay: 0.4s;
            }
          }
        }
      }
    }
  }
}

.input-area {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;

  input {
    flex: 1;
    padding: 12px 16px;
    border: 2px solid #e8e8e8;
    border-radius: 24px;
    font-size: 15px;
    outline: none;
    transition: border-color 0.3s;

    &:focus {
      border-color: #1890ff;
    }
  }

  button {
    padding: 0 32px;
    background: #1890ff;
    color: white;
    border: none;
    border-radius: 24px;
    cursor: pointer;
    font-size: 15px;
    font-weight: 500;
    transition: background 0.3s;

    &:hover:not(:disabled) {
      background: #40a9ff;
    }

    &:disabled {
      background: #d9d9d9;
      cursor: not-allowed;
    }
  }
}

.quick-questions {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0f0f0;

  p {
    font-size: 13px;
    color: #999;
    margin-bottom: 12px;
  }

  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .quick-q {
    padding: 8px 16px;
    background: #f5f5f5;
    border: none;
    border-radius: 16px;
    cursor: pointer;
    font-size: 13px;
    transition: all 0.3s;

    &:hover {
      background: #e6f7ff;
      color: #1890ff;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 150px);
  }

  .messages-container {
    .message {
      .message-content {
        max-width: 90%;
      }
    }
  }
}
</style>
