import request from '@/utils/request'

// 分页查询试卷题目关联列表
export function pageEduExamPaperQuestion(current, size, params) {
  return request({
    url: `/edu_exam_paper_question/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询试卷题目关联列表（不分页）
export function listEduExamPaperQuestion(params) {
  return request({
    url: '/edu_exam_paper_question/list',
    method: 'post',
    data: params || {}
  })
}

// 查询试卷题目关联详情
export function getEduExamPaperQuestionById(id) {
  return request({
    url: '/edu_exam_paper_question/' + id,
    method: 'get'
  })
}

// 新增试卷题目关联
export function saveEduExamPaperQuestion(data) {
  return request({
    url: '/edu_exam_paper_question',
    method: 'post',
    data: data
  })
}

// 修改试卷题目关联
export function updateEduExamPaperQuestion(data) {
  return request({
    url: '/edu_exam_paper_question',
    method: 'put',
    data: data
  })
}

// 删除试卷题目关联
export function deleteEduExamPaperQuestion(ids) {
  return request({
    url: '/edu_exam_paper_question/' + ids,
    method: 'delete'
  })
}

// 批量删除试卷题目关联
export function deleteBatchEduExamPaperQuestion(ids) {
  return request({
    url: '/edu_exam_paper_question/batch',
    method: 'delete',
    data: ids
  })
}

// AI生成试卷题目
export function aiGeneratePaperQuestions(data) {
  return request({
    url: '/edu_exam_paper_question/ai-generate',
    method: 'post',
    data: data
  })
}
