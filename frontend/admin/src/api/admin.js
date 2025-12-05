import request from '@/utils/request'

// 获取统计数据
export function getStatistics() {
  return request.get('/admin/tools/statistics')
}

// 工具管理
export function getToolsList(params) {
  return request.post('/admin/tools/query', params)
}

export function approveTool(id) {
  return request.put(`/admin/tools/${id}/approve`)
}

export function rejectTool(id, reason) {
  return request.put(`/admin/tools/${id}/reject`, { reason })
}

export function deleteTool(id) {
  return request.delete(`/admin/tools/${id}`)
}

// 用户管理 (使用普通用户接口，需要管理员权限)
export function getUsersList(params) {
  return request.get('/users', { params })
}

// 评论管理
export function getReviewsList(params) {
  return request.get('/reviews', { params })
}

export function deleteReview(id) {
  return request.delete(`/reviews/${id}`)
}

// 分类管理
export function getCategoriesList() {
  return request.get('/categories')
}

export function createCategory(data) {
  return request.post('/categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
