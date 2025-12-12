import request from './request'

// 获取工具的评论列表
export function getReviews(params) {
  const { toolId, ...otherParams } = params
  return request({
    url: `/reviews/tool/${toolId}`,
    method: 'get',
    params: otherParams
  })
}

// 创建评论
export function createReview(data) {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

// 删除评论
export function deleteReview(id) {
  return request({
    url: `/reviews/${id}`,
    method: 'delete'
  })
}

// 获取我的评论
export function getMyReviews(params) {
  return request({
    url: '/reviews/my',
    method: 'get',
    params
  })
}

// 获取工具的评论树形结构（含回复）
export function getReviewsTree(toolId) {
  return request({
    url: `/reviews/tool/${toolId}/tree`,
    method: 'get'
  })
}

// 获取某条评论的回复列表
export function getReviewReplies(reviewId, params) {
  return request({
    url: `/reviews/${reviewId}/replies`,
    method: 'get',
    params
  })
}

// 标记评论有帮助
export function markHelpful(reviewId) {
  return request({
    url: `/reviews/${reviewId}/helpful`,
    method: 'post'
  })
}

// 取消有帮助标记
export function unmarkHelpful(reviewId) {
  return request({
    url: `/reviews/${reviewId}/helpful`,
    method: 'delete'
  })
}
