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
        console.log('===== 响应拦截器 =====')
        console.log('完整响应:', response)
        console.log('响应数据:', response.data)
        console.log('响应类型:', response.config?.responseType)

        // 如果是 blob 响应（如验证码图片），直接返回
        if (response.config.responseType === 'blob' || response.data instanceof Blob) {
            console.log('这是 blob 响应，直接返回')
            return response.data
        }

        const res = response.data
        console.log('检查 res.code:', res.code)

        if (res.code !== 200) {
            console.log('响应 code 不是 200，拒绝')
            console.log('错误消息:', res.msg || res.message)
            Message({
                message: res.msg || res.message || '系统错误',
                type: 'error',
                duration: 2 * 1000
            })
            // 学生端 401：清除学生 token 并跳转登录
            if (res.code === 401 && response.config.url && response.config.url.indexOf('/student') !== -1) {
                store.dispatch('student/resetToken')
                router.replace('/student/login')
            }
            return Promise.reject(new Error(res.msg || res.message || 'Error'))
        } else {
            console.log('响应成功，返回 res')
            return res
        }
    },
    error => {
        console.log('===== 请求错误 =====')
        console.log('错误对象:', error)
        console.log('错误响应:', error.response)
        console.log('错误请求:', error.request)

        // 如果有响应且返回了错误信息
        if (error.response) {
            const res = error.response.data
            console.log('错误响应数据:', res)
            if (res && (res.code === 500 || res.code === 400 || res.code === 404)) {
                console.log('后端错误消息:', res.msg || res.message)
                Message({
                    message: res.msg || res.message || '系统错误',
                    type: 'error',
                    duration: 2 * 1000
                })
            }
        }

        return Promise.reject(error)
    }
)

export default service
