import request from './request'
import { getMockTools } from './mock'

// 是否使用Mock数据（当后端未启动时自动使用）
const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// 获取工具列表
export async function getTools(queryDTO) {
  // 如果强制使用Mock或后端请求失败，使用Mock数据
  try {
    const res = await request({
      url: '/tools/query',
      method: 'post',
      data: queryDTO
    })
    return res
  } catch (error) {
    console.warn('后端API调用失败，使用Mock数据:', error.message)
    // 使用Mock数据
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(getMockTools(
          queryDTO.current || 1,
          queryDTO.size || 12,
          queryDTO.categoryId,
          queryDTO.sortBy || 'upvote'
        ))
      }, 300) // 模拟网络延迟
    })
  }
}

// 获取工具详情
export function getToolDetail(id) {
  return request({
    url: `/tools/${id}`,
    method: 'get'
  })
}

// 获取热门工具
export function getHotTools(limit = 10) {
  return request({
    url: '/tools/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取最新工具
export function getLatestTools(limit = 10) {
  return request({
    url: '/tools/latest',
    method: 'get',
    params: { limit }
  })
}

// 搜索工具
export function searchTools(keyword) {
  return request({
    url: '/tools/search',
    method: 'get',
    params: { keyword }
  })
}

// 点赞工具
export function upvoteTool(id) {
  return request({
    url: `/tools/${id}/upvote`,
    method: 'post'
  })
}

// 获取所有分类
export function getCategories() {
  return request({
    url: '/categories',
    method: 'get'
  })
}
