import request from '@/utils/request'

// 分页查询章节学习内容列表（current、size 放路径参数，其余放 body）
export function pageEduChapterContent(current, size, params) {
  return request({
    url: `/edu_chapter_content/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询章节学习内容详情
export function getEduChapterContentById(id) {
  return request({
    url: '/edu_chapter_content/' + id,
    method: 'get'
  })
}

// 新增章节学习内容
export function saveEduChapterContent(data) {
  return request({
    url: '/edu_chapter_content',
    method: 'post',
    data: data
  })
}

// 修改章节学习内容
export function updateEduChapterContent(data) {
  return request({
    url: '/edu_chapter_content',
    method: 'put',
    data: data
  })
}

// 删除章节学习内容
export function deleteEduChapterContent(ids) {
  return request({
    url: '/edu_chapter_content/' + ids,
    method: 'delete'
  })
}

// 批量删除章节学习内容
export function deleteBatchEduChapterContent(ids) {
  return request({
    url: '/edu_chapter_content/batch',
    method: 'delete',
    data: ids
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
