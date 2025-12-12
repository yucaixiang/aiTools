import axios from 'axios'
import toast from '@/utils/toast'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 检查业务code，如果不是200则认为是业务错误
    if (res.code !== undefined && res.code !== 200) {
      console.log('========== 业务错误拦截 ==========')
      console.log('Code:', res.code)
      console.log('Message:', res.message)

      const errorMessage = res.message || '请求失败'

      // 显示错误提示
      try {
        toast.error(errorMessage)
        console.log('Toast.error调用成功:', errorMessage)
      } catch (e) {
        console.error('Toast.error调用失败:', e)
      }

      // 如果是401业务错误，也要清除登录状态
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      }

      // 抛出业务错误
      const businessError = new Error(errorMessage)
      businessError.code = res.code
      businessError.data = res.data
      return Promise.reject(businessError)
    }

    // 业务成功，返回数据
    return res
  },
  (error) => {
    console.log('========== 请求错误拦截 ==========')
    console.log('Error:', error)
    console.log('Response:', error.response)
    console.log('Status:', error.response?.status)

    // 提取后端返回的错误消息
    const errorMessage = error.response?.data?.message || error.message || '请求失败'
    const statusCode = error.response?.status

    // 处理401未授权错误
    if (statusCode === 401) {
      console.log('检测到401错误，显示Toast提示')

      // 显示未授权提示（Toast弹窗）
      try {
        toast.error('未授权，请先登录')
        console.log('Toast.error调用成功')
      } catch (e) {
        console.error('Toast.error调用失败:', e)
      }

      // 清除登录状态
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      console.log('已清除登录信息')

      // 延迟跳转到登录页，让用户看到提示
      setTimeout(() => {
        console.log('准备跳转到登录页')
        window.location.href = '/login'
      }, 1500)

      const authError = new Error('未授权，请先登录')
      authError.code = 401
      authError.response = error.response
      return Promise.reject(authError)
    }

    // 处理其他错误 - 显示错误提示（Toast弹窗）
    console.log('其他错误，显示Toast:', errorMessage)
    try {
      toast.error(errorMessage)
      console.log('Toast.error调用成功')
    } catch (e) {
      console.error('Toast.error调用失败:', e)
    }

    console.error('API错误:', errorMessage)

    // 创建一个新的错误对象，包含后端的业务消息
    const businessError = new Error(errorMessage)
    businessError.code = error.response?.data?.code || statusCode
    businessError.response = error.response

    return Promise.reject(businessError)
  }
)

export default request
