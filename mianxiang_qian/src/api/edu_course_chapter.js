import request from '@/utils/request'

// 分页查询课程章节列表（current、size 放路径参数，其余放 body）
export function pageEduCourseChapter(current, size, params) {
  return request({
    url: `/edu_course_chapter/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询课程章节详情
export function getEduCourseChapterById(id) {
  return request({
    url: '/edu_course_chapter/' + id,
    method: 'get'
  })
}

// 新增课程章节
export function saveEduCourseChapter(data) {
  return request({
    url: '/edu_course_chapter',
    method: 'post',
    data: data
  })
}

// 修改课程章节
export function updateEduCourseChapter(data) {
  return request({
    url: '/edu_course_chapter',
    method: 'put',
    data: data
  })
}

// 删除课程章节
export function deleteEduCourseChapter(ids) {
  return request({
    url: '/edu_course_chapter/' + ids,
    method: 'delete'
  })
}

// 批量删除课程章节
export function deleteBatchEduCourseChapter(ids) {
  return request({
    url: '/edu_course_chapter/batch',
    method: 'delete',
    data: ids
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

// 查询课程章节列表（不分页）
export function listEduCourseChapter(data) {
  return request({
    url: '/edu_course_chapter/list',
    method: 'post',
    data: data || {}
  })
}
