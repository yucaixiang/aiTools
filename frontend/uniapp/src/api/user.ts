/**
 * 用户相关API
 */
import { get, post } from '../utils/request'

export interface User {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl: string
  bio: string
  role: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

/**
 * 用户注册
 */
export function register(data: {
  username: string
  email: string
  password: string
  nickname?: string
}) {
  return post<User>('/api/users/register', data)
}

/**
 * 用户登录
 */
export function login(data: {
  account: string
  password: string
}) {
  return post<LoginResult>('/api/users/login', data)
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return get<User>('/api/users/me')
}

/**
 * 检查用户名是否存在
 */
export function checkUsername(username: string) {
  return get<boolean>(`/api/users/check-username?username=${username}`)
}

/**
 * 检查邮箱是否存在
 */
export function checkEmail(email: string) {
  return get<boolean>(`/api/users/check-email?email=${email}`)
}
