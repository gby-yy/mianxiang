import request from '@/utils/request'

// 分页查询课程列表（current、size 放路径参数，其余放 body）
export function pageEduCourse(current, size, params) {
  return request({
    url: `/edu_course/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询课程详情
export function getEduCourseById(id) {
  return request({
    url: '/edu_course/' + id,
    method: 'get'
  })
}

// 新增课程
export function saveEduCourse(data) {
  return request({
    url: '/edu_course',
    method: 'post',
    data: data
  })
}

// 修改课程
export function updateEduCourse(data) {
  return request({
    url: '/edu_course',
    method: 'put',
    data: data
  })
}

// 删除课程
export function deleteEduCourse(ids) {
  return request({
    url: '/edu_course/' + ids,
    method: 'delete'
  })
}

// 批量删除课程
export function deleteBatchEduCourse(ids) {
  return request({
    url: '/edu_course/batch',
    method: 'delete',
    data: ids
  })
}


// 下拉选项：课程教师
export function listEduUser(data) {
  return request({
    url: '/edu_user/list',
    method: 'post',
    data: data || {}
  })
}
