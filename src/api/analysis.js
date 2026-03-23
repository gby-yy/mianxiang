import request from '@/utils/request'

/**
 * 获取当前用户可见的课程列表（教师本人课程，管理员全部）
 */
export function getMyCourses() {
  return request({
    url: '/analysis/my-courses',
    method: 'get'
  })
}

/**
 * 获取某课程的详细统计数据
 * @param courseId 课程ID
 * @returns { courseName, studentCount, avgProgressRate, totalExamCount, overallPassRate, avgScore, chapterStats[], studentProgress[] }
 */
export function getCourseStats(courseId) {
  return request({
    url: `/analysis/course/${courseId}/stats`,
    method: 'get'
  })
}
