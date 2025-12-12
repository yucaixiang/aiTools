import request from './request'

// 创建代找/代开发请求
export function createHelpRequest(data) {
  return request({
    url: '/help-requests',
    method: 'post',
    data
  })
}

// 获取我的请求列表
export function getMyRequests(params) {
  return request({
    url: '/help-requests/my',
    method: 'get',
    params
  })
}

// 获取请求详情
export function getRequestDetail(id) {
  return request({
    url: `/help-requests/${id}`,
    method: 'get'
  })
}
