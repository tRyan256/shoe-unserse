import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    
    // 如果存在token，添加到请求头
    if (userStore.token) {
      config.headers['token'] = userStore.token
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的是文件流，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 业务状态码判断
    if (res.code === 1) {
      // 成功
      return res.data
    } else if (res.code === 0) {
      // 业务错误
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    } else if (res.code === 401) {
      // 未授权，token过期或无效
      ElMessageBox.confirm(
        '登录状态已过期，请重新登录',
        '系统提示',
        {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        router.push({ name: 'Login' })
      }).catch(() => {})
      
      return Promise.reject(new Error('登录已过期'))
    } else {
      // 其他错误
      ElMessage.error(res.msg || '未知错误')
      return Promise.reject(new Error(res.msg || '未知错误'))
    }
  },
  (error) => {
    console.error('响应错误:', error)
    
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data?.msg || '请求失败'
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
