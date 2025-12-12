import request from './request'

// 添加收藏
export function addFavorite(toolId) {
  return request({
    url: `/favorites/${toolId}`,
    method: 'post'
  })
}

// 取消收藏
export function removeFavorite(toolId) {
  return request({
    url: `/favorites/${toolId}`,
    method: 'delete'
  })
}

// 检查是否已收藏
export function checkFavorite(toolId) {
  return request({
    url: `/favorites/${toolId}/check`,
    method: 'get'
  })
}

// 获取我的收藏列表
export function getMyFavorites(params) {
  return request({
    url: '/favorites/my',
    method: 'get',
    params
  })
}
