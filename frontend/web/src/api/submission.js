import request from './request'

// 提交工具
export function submitTool(data) {
  return request({
    url: '/submissions',
    method: 'post',
    data
  })
}

// 获取我提交的工具
export function getMySubmissions(params) {
  return request({
    url: '/submissions/my',
    method: 'get',
    params
  })
}

// 获取提交详情
export function getSubmissionDetail(id) {
  return request({
    url: `/submissions/${id}`,
    method: 'get'
  })
}

// 管理员：获取所有提交列表（带筛选）
export function getAllSubmissions(params) {
  return request({
    url: '/submissions',
    method: 'get',
    params
  })
}

// 管理员：审核提交
export function reviewSubmission(id, data) {
  return request({
    url: `/submissions/${id}/review`,
    method: 'put',
    data
  })
}

// 获取待审核数量
export function getPendingCount() {
  return request({
    url: '/submissions/stats/pending',
    method: 'get'
  })
}
