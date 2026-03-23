import request from '@/utils/request'

// 分页查询学生考试记录列表（current、size 放路径参数，其余放 body）
export function pageEduStudentExamRecord(current, size, params) {
  return request({
    url: `/edu_student_exam_record/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询学生考试记录详情
export function getEduStudentExamRecordById(id) {
  return request({
    url: '/edu_student_exam_record/' + id,
    method: 'get'
  })
}

// 查询某条考试记录的学生提交记录（答题详情）
export function getExamRecordDetail(id) {
  return request({
    url: '/edu_student_exam_record/' + id + '/detail',
    method: 'get'
  })
}

// 新增学生考试记录
export function saveEduStudentExamRecord(data) {
  return request({
    url: '/edu_student_exam_record',
    method: 'post',
    data: data
  })
}

// 修改学生考试记录
export function updateEduStudentExamRecord(data) {
  return request({
    url: '/edu_student_exam_record',
    method: 'put',
    data: data
  })
}

// 删除学生考试记录
export function deleteEduStudentExamRecord(ids) {
  return request({
    url: '/edu_student_exam_record/' + ids,
    method: 'delete'
  })
}

// 批量删除学生考试记录
export function deleteBatchEduStudentExamRecord(ids) {
  return request({
    url: '/edu_student_exam_record/batch',
    method: 'delete',
    data: ids
  })
}


// 下拉选项：考试试卷
export function listEduChapterExamPaper(data) {
  return request({
    url: '/edu_chapter_exam_paper/list',
    method: 'post',
    data: data || {}
  })
}

// 下拉选项：学生
export function listEduStudentUser(data) {
  return request({
    url: '/edu_student_user/list',
    method: 'post',
    data: data || {}
  })
}
