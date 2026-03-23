import request from '@/utils/request'

// 分页查询系统用户列表（current、size 放路径参数，其余放 body）
export function pageEduUser(current, size, params) {
  return request({
    url: `/edu_user/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询系统用户详情
export function getEduUserById(id) {
  return request({
    url: '/edu_user/' + id,
    method: 'get'
  })
}

// 新增系统用户
export function saveEduUser(data) {
  return request({
    url: '/edu_user',
    method: 'post',
    data: data
  })
}

// 修改系统用户
export function updateEduUser(data) {
  return request({
    url: '/edu_user',
    method: 'put',
    data: data
  })
}

// 删除系统用户
export function deleteEduUser(ids) {
  return request({
    url: '/edu_user/' + ids,
    method: 'delete'
  })
}

// 批量删除系统用户
export function deleteBatchEduUser(ids) {
  return request({
    url: '/edu_user/batch',
    method: 'delete',
    data: ids
  })
}
