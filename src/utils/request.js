import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'
import { getToken, getStudentToken } from '@/utils/auth'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // 请求地址前缀
  timeout: 20000 // 响应超时时间
})

// 请求拦截器：学生端接口用学生 token，其余用管理端 token
service.interceptors.request.use(
  config => {
    const isStudentApi = config.url && config.url.indexOf('/student') !== -1
    if (isStudentApi) {
      const token = getStudentToken()
      if (token) {
        config.headers['Authorization'] = token
      }
    } else {
      if (store.getters.token) {
        config.headers['Authorization'] = getToken()
      }
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果是 blob 响应（如验证码图片），直接返回
    if (response.config.responseType === 'blob' || response.data instanceof Blob) {
      return response.data
    }

    const res = response.data
    if (res.code !== 200) {
      Message({
        message: res.msg,
        type: 'error',
        duration: 2 * 1000
      })
      // 学生端 401：清除学生 token 并跳转登录
      if (res.code === 401 && response.config.url && response.config.url.indexOf('/student') !== -1) {
        store.dispatch('student/resetToken')
        router.replace('/student/login')
      }
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res
    }
  },
  error => {
    return Promise.reject(error)
  }
)

export default service
