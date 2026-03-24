import request from '@/utils/request'

// 分页查询章节题库列表（current、size 放路径参数，其余放 body）
export function pageEduQuestionBank(current, size, params) {
  return request({
    url: `/edu_question_bank/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询章节题库详情
export function getEduQuestionBankById(id) {
  return request({
    url: '/edu_question_bank/' + id,
    method: 'get'
  })
}

// 新增章节题库
export function saveEduQuestionBank(data) {
  return request({
    url: '/edu_question_bank',
    method: 'post',
    data: data
  })
}

// 修改章节题库
export function updateEduQuestionBank(data) {
  return request({
    url: '/edu_question_bank',
    method: 'put',
    data: data
  })
}

// 删除章节题库
export function deleteEduQuestionBank(ids) {
  return request({
    url: '/edu_question_bank/' + ids,
    method: 'delete'
  })
}

// 批量删除章节题库
export function deleteBatchEduQuestionBank(ids) {
  return request({
    url: '/edu_question_bank/batch',
    method: 'delete',
    data: ids
  })
}

// 查询所有章节题库列表（不分页）
export function listEduQuestionBank(params) {
  return request({
    url: '/edu_question_bank/list',
    method: 'post',
    data: params || {}
  })
}

// 下拉选项：所属课程
export function listEduCourse(data) {
  return request({
    url: '/edu_course/list',
    method: 'post',
    data: data || {}
  })
}

// 下拉选项：所属章节
export function listEduCourseChapter(data) {
  return request({
    url: '/edu_course_chapter/list',
    method: 'post',
    data: data || {}
  })
}

// Excel 批量导入题目
export function importEduQuestionBank(file, courseId, chapterId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseId', courseId)
  formData.append('chapterId', chapterId)

  return request({
    url: '/edu_question_bank/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}