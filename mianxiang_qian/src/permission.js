import router from './router'
import store from './store'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken, getStudentToken } from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'
import { addRoutesByRoles, constantRoutes, resetRouter } from './router'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

// 管理端白名单
const whiteList = ['/login']
// 学生端白名单
const studentWhiteList = ['/student/login', '/student/register']

router.beforeEach(async(to, from, next) => {
  NProgress.start()
  document.title = getPageTitle(to.meta.title)

  // ========== 学生端路由：独立 token 校验，与管理端完全隔离 ==========
  if (to.path.startsWith('/student')) {
    const hasStudentToken = getStudentToken()
    if (hasStudentToken) {
      if (to.path === '/student/login' || to.path === '/student/register') {
        next({ path: '/student/index', replace: true })
        NProgress.done()
      } else {
        next()
      }
    } else {
      if (studentWhiteList.indexOf(to.path) !== -1) {
        next()
      } else {
        next('/student/login')
        NProgress.done()
      }
    }
    return
  }

  // ========== 以下为管理端路由 ==========
  if (to.path === '/login' && from.path.startsWith('/student')) {
    next()
    return
  }
  const hasToken = getToken()
  if (hasToken) {
    if (to.path === '/login') {
      // 最强制的方法：清除所有状态，确保重新加载
      await store.dispatch('app/setDynamicRoutes', [])
      store.commit('app/SET_CURRENT_ROUTE_ROLE', '')
      // 清除用户信息，强制重新获取
      store.commit('user/RESET_STATE')
      // 重新设置 token（因为 RESET_STATE 会清除 token）
      const token = getToken()
      if (token) {
        store.commit('user/SET_TOKEN', token)
      }
      resetRouter()
      // 直接去首页，路由守卫会强制重新获取用户信息和路由
      next({ path: '/' })
      // 关闭进度条
      NProgress.done()
    } else {
      // 有token，强制检查并重新加载路由（最强制的方法）
      const hasGetUserInfo = store.getters.name
      const currentRole = store.getters.role
      const currentDynamicRoutes = store.getters.dynamicRoutes || []
      const storedRouteRole = store.state.app.currentRouteRole

      // 最强制的方法：每次路由守卫都检查角色是否匹配，不匹配就强制重新加载
      // 如果没有用户信息，或者角色不匹配，或者动态路由为空，都强制重新加载
      const needsReload = !hasGetUserInfo ||
          !currentRole ||
          currentDynamicRoutes.length === 0 ||
          storedRouteRole !== currentRole

      if (needsReload) {
        // 最强制的方法：强制重新加载路由
        try {
          // 先清除旧的动态路由和路由
          await store.dispatch('app/setDynamicRoutes', [])
          store.commit('app/SET_CURRENT_ROUTE_ROLE', '')
          resetRouter()

          // 如果没有用户信息，先获取用户信息
          if (!hasGetUserInfo || !currentRole) {
            await store.dispatch('user/getInfo')
          }

          const role = store.getters.role

          if (!role) {
            throw new Error('获取角色信息失败')
          }

          // 根据新角色加载路由
          const dynamicRoutes = addRoutesByRoles([role])
          // 合并常量路由和动态路由
          const allRoutes = [...constantRoutes, ...dynamicRoutes]
          // 设置动态路由
          await store.dispatch('app/setDynamicRoutes', allRoutes)
          // 保存当前路由对应的角色
          store.commit('app/SET_CURRENT_ROUTE_ROLE', role)

          // 最强制的方法：如果检测到角色变化，强制刷新页面确保完全重新加载
          if (storedRouteRole && storedRouteRole !== role) {
            // 角色变化了，强制刷新页面
            window.location.reload()
            return
          }

          // 使用 replace: true 确保路由正确更新
          next({ ...to, replace: true })
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 获取用户信息失败，就需要重置token
          await store.dispatch('user/resetToken')
          await store.dispatch('app/setDynamicRoutes', [])
          store.commit('app/SET_CURRENT_ROUTE_ROLE', '')
          next(`/login`)
          NProgress.done()
        }
      } else {
        // 有用户信息且动态路由已加载，且角色匹配，直接放行
        next()
      }
    }
  } else {
    // 没有管理端 token：根路径去学生登录，白名单放行，否则去管理端登录
    if (to.path === '/') {
      next('/student/login')
      NProgress.done()
    } else if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // 关闭进度条
  NProgress.done()
})
