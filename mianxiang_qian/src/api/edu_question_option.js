import request from '@/utils/request'

// 分页查询题目选项列表
export function pageEduQuestionOption(current, size, params) {
  return request({
    url: `/edu_question_option/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询题目选项列表（不分页）
export function listEduQuestionOption(params) {
  return request({
    url: '/edu_question_option/list',
    method: 'post',
    data: params || {}
  })
}

// 查询题目选项详情
export function getEduQuestionOptionById(id) {
  return request({
    url: '/edu_question_option/' + id,
    method: 'get'
  })
}

// 新增题目选项
export function saveEduQuestionOption(data) {
  return request({
    url: '/edu_question_option',
    method: 'post',
    data: data
  })
}

// 修改题目选项
export function updateEduQuestionOption(data) {
  return request({
    url: '/edu_question_option',
    method: 'put',
    data: data
  })
}

// 删除题目选项
export function deleteEduQuestionOption(ids) {
  return request({
    url: '/edu_question_option/' + ids,
    method: 'delete'
  })
}

// 批量删除题目选项
export function deleteBatchEduQuestionOption(ids) {
  return request({
    url: '/edu_question_option/batch',
    method: 'delete',
    data: ids
  })
}
