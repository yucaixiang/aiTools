/**
 * AI相关API
 */
import { get, post, del } from '../utils/request'
import type { Tool } from './tool'

export interface ChatRequest {
  sessionId?: string
  message: string
  needRecommendation?: boolean
  recommendCount?: number
}

export interface ChatResponse {
  sessionId: string
  message: string
  recommendedTools: Tool[]
  intent: string
  responseTime: number
}

/**
 * 发送聊天消息
 */
export function sendChat(data: ChatRequest) {
  return post<ChatResponse>('/api/ai/chat', data)
}

/**
 * 获取会话历史
 */
export function getChatHistory(sessionId: string) {
  return get<ChatResponse[]>(`/api/ai/chat/history/${sessionId}`)
}

/**
 * 清除会话历史
 */
export function clearChatHistory(sessionId: string) {
  return del(`/api/ai/chat/history/${sessionId}`)
}

/**
 * 创建新会话
 */
export function createSession() {
  return post<string>('/api/ai/chat/session')
}

/**
 * 工具推荐
 */
export function recommendTools(query: string, limit: number = 5) {
  return get<Tool[]>(`/api/ai/recommend?query=${query}&limit=${limit}`)
}

/**
 * 个性化推荐
 */
export function getPersonalizedRecommend(limit: number = 10) {
  return get<Tool[]>(`/api/ai/recommend/personalized?limit=${limit}`)
}
