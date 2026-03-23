import request from '@/utils/request'

// 系统用户角色分布统计
export function countUserByRoleType(params) {
  return request({
    url: '/echarts/countUserByRoleType',
    method: 'post',
    data: params
  })
}

// 课程难度分布统计
export function countCourseByDifficulty(params) {
  return request({
    url: '/echarts/countCourseByDifficulty',
    method: 'post',
    data: params
  })
}

// 题目类型分布统计
export function countQuestionByType(params) {
  return request({
    url: '/echarts/countQuestionByType',
    method: 'post',
    data: params
  })
}

// 考试提交状态统计
export function countExamRecordBySubmitStatus(params) {
  return request({
    url: '/echarts/countExamRecordBySubmitStatus',
    method: 'post',
    data: params
  })
}

// 不同难度试卷平均分统计
export function avgExamScoreByPaperDifficulty(params) {
  return request({
    url: '/echarts/avgExamScoreByPaperDifficulty',
    method: 'post',
    data: params
  })
}

// 章节内容类型分布统计
export function countContentByContentType(params) {
  return request({
    url: '/echarts/countContentByContentType',
    method: 'post',
    data: params
  })
}

// 试卷难度分布统计（全平台）
export function countPaperByDifficulty() {
  return request({
    url: '/echarts/countPaperByDifficulty',
    method: 'post',
    data: {}
  })
}

