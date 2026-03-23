import request from '@/utils/request'

/**
 * 首页统计数据：管理员为全局，教师为本人
 */
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

/**
 * 管理员数据总览：总学生数、总教师数、课程数量、学习总人次、平均通过率
 */
export function getDashboardOverview() {
  return request({
    url: '/dashboard/overview',
    method: 'get'
  })
}
