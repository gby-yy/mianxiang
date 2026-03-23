import request from '@/utils/request'

// 分页查询学生列表（current、size 放路径参数，其余放 body）
export function pageEduStudentUser(current, size, params) {
  return request({
    url: `/edu_student_user/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询学生详情
export function getEduStudentUserById(id) {
  return request({
    url: '/edu_student_user/' + id,
    method: 'get'
  })
}

// 新增学生
export function saveEduStudentUser(data) {
  return request({
    url: '/edu_student_user',
    method: 'post',
    data: data
  })
}

// 修改学生
export function updateEduStudentUser(data) {
  return request({
    url: '/edu_student_user',
    method: 'put',
    data: data
  })
}

// 删除学生
export function deleteEduStudentUser(ids) {
  return request({
    url: '/edu_student_user/' + ids,
    method: 'delete'
  })
}

// 批量删除学生
export function deleteBatchEduStudentUser(ids) {
  return request({
    url: '/edu_student_user/batch',
    method: 'delete',
    data: ids
  })
}
