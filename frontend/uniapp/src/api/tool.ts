/**
 * 工具相关API
 */
import { get, post, put, del } from '../utils/request'

export interface Tool {
  id: number
  name: string
  slug: string
  tagline: string
  logoUrl: string
  websiteUrl: string
  categoryName: string
  pricingModel: string
  startingPrice: number
  viewCount: number
  favoriteCount: number
  upvoteCount: number
  reviewCount: number
  averageRating: number
  tags: Tag[]
  isFavorited: boolean
  isUpvoted: boolean
}

export interface ToolDetail extends Tool {
  description: string
  categoryId: number
  pricingDetails?: string
  features?: string[]
  hasUpvoted: boolean
  hasFavorited: boolean
}

export interface Tag {
  id: number
  name: string
  slug: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
  hasNext: boolean
  hasPrevious: boolean
}

/**
 * 查询热门工具
 */
export function getHotTools(limit: number = 10) {
  return get<Tool[]>(`/api/tools/hot?limit=${limit}`)
}

/**
 * 查询最新工具
 */
export function getLatestTools(limit: number = 10) {
  return get<Tool[]>(`/api/tools/latest?limit=${limit}`)
}

/**
 * 搜索工具
 */
export function searchTools(keyword: string, current: number = 1, size: number = 20) {
  return get<PageResult<Tool>>(`/api/tools/search?keyword=${keyword}&current=${current}&size=${size}`)
}

/**
 * 查询工具详情
 */
export function getToolDetail(id: number) {
  return get<ToolDetail>(`/api/tools/${id}`)
}

/**
 * 点赞工具
 */
export function upvoteTool(id: number) {
  return post(`/api/tools/${id}/upvote`)
}

/**
 * 取消点赞
 */
export function cancelUpvote(id: number) {
  return del(`/api/tools/${id}/upvote`)
}

/**
 * 收藏工具
 */
export function favoriteTool(id: number) {
  return post(`/api/tools/${id}/favorite`)
}

/**
 * 取消收藏
 */
export function cancelFavorite(id: number) {
  return del(`/api/tools/${id}/favorite`)
}

/**
 * 分页查询工具
 */
export function queryTools(params: any) {
  return post<PageResult<Tool>>('/api/tools/query', params)
}

/**
 * 查询相似工具
 */
export function getSimilarTools(toolId: number, limit: number = 5) {
  return get<Tool[]>(`/api/tools/${toolId}/similar?limit=${limit}`)
}
