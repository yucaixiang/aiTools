import { get, post, put, del } from '../utils/request'

/**
 * Review interfaces
 */
export interface Review {
  id: number
  toolId: number
  userId: number
  rating: number
  content: string
  pros: string[]
  cons: string[]
  helpfulCount: number
  hasMarkedHelpful: boolean
  status: number
  createdAt: string
  updatedAt: string
  user?: {
    id: number
    username: string
    avatar: string
  }
}

export interface ReviewCreateDTO {
  toolId: number
  rating: number
  content: string
  pros?: string[]
  cons?: string[]
}

export interface RatingDistribution {
  distribution: Array<{
    rating: number
    count: number
  }>
  totalCount: number
  averageRating: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/**
 * Create a review
 */
export function createReview(data: ReviewCreateDTO) {
  return post<number>('/api/reviews', data)
}

/**
 * Update a review
 */
export function updateReview(id: number, data: ReviewCreateDTO) {
  return put<void>(`/api/reviews/${id}`, data)
}

/**
 * Delete a review
 */
export function deleteReview(id: number) {
  return del<void>(`/api/reviews/${id}`)
}

/**
 * Get tool reviews with pagination
 */
export function getToolReviews(toolId: number, current: number = 1, size: number = 10) {
  return get<PageResult<Review>>(`/api/reviews/tool/${toolId}?current=${current}&size=${size}`)
}

/**
 * Get user reviews with pagination
 */
export function getUserReviews(userId: number, current: number = 1, size: number = 10) {
  return get<PageResult<Review>>(`/api/reviews/user/${userId}?current=${current}&size=${size}`)
}

/**
 * Get hot reviews for a tool
 */
export function getHotReviews(toolId: number, limit: number = 3) {
  return get<Review[]>(`/api/reviews/tool/${toolId}/hot?limit=${limit}`)
}

/**
 * Mark a review as helpful
 */
export function markHelpful(reviewId: number) {
  return post<void>(`/api/reviews/${reviewId}/helpful`)
}

/**
 * Unmark a review as helpful
 */
export function unmarkHelpful(reviewId: number) {
  return del<void>(`/api/reviews/${reviewId}/helpful`)
}

/**
 * Check if user has reviewed a tool
 */
export function hasReviewed(toolId: number) {
  return get<boolean>(`/api/reviews/check?toolId=${toolId}`)
}

/**
 * Get rating distribution for a tool
 */
export function getRatingDistribution(toolId: number) {
  return get<RatingDistribution>(`/api/reviews/tool/${toolId}/rating-distribution`)
}
