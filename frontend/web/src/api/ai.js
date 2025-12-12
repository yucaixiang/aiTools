import request from './request'

// AI聊天
export function chat(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

// AI推荐工具
export function getRecommendations(data) {
  return request({
    url: '/ai/recommend',
    method: 'post',
    data
  })
}
