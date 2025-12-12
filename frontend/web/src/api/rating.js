import request from './request'

/**
 * 提交评分
 */
export function submitRating(data) {
  return request({
    url: '/ratings',
    method: 'post',
    data
  })
}

/**
 * 获取工具评分统计
 */
export function getToolRatingStats(toolId) {
  return request({
    url: `/ratings/tool/${toolId}`,
    method: 'get'
  })
}

/**
 * 删除评分
 */
export function deleteRating(toolId) {
  return request({
    url: `/ratings/tool/${toolId}`,
    method: 'delete'
  })
}

/**
 * 检查是否已评分
 */
export function checkRating(toolId) {
  return request({
    url: `/ratings/tool/${toolId}/check`,
    method: 'get'
  })
}
