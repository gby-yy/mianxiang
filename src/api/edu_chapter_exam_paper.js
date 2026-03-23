import request from '@/utils/request'

// 分页查询章节试卷列表（current、size 放路径参数，其余放 body）
export function pageEduChapterExamPaper(current, size, params) {
  return request({
    url: `/edu_chapter_exam_paper/page?current=${current}&size=${size}`,
    method: 'post',
    data: params
  })
}

// 查询章节试卷列表（不分页）
export function listEduChapterExamPaper(params) {
  return request({
    url: '/edu_chapter_exam_paper/list',
    method: 'post',
    data: params || {}
  })
}

// 查询章节试卷详情
export function getEduChapterExamPaperById(id) {
  return request({
    url: '/edu_chapter_exam_paper/' + id,
    method: 'get'
  })
}

// 新增章节试卷
export function saveEduChapterExamPaper(data) {
  return request({
    url: '/edu_chapter_exam_paper',
    method: 'post',
    data: data
  })
}

// 修改章节试卷
export function updateEduChapterExamPaper(data) {
  return request({
    url: '/edu_chapter_exam_paper',
    method: 'put',
    data: data
  })
}

// 删除章节试卷
export function deleteEduChapterExamPaper(ids) {
  return request({
    url: '/edu_chapter_exam_paper/' + ids,
    method: 'delete'
  })
}

// 批量删除章节试卷
export function deleteBatchEduChapterExamPaper(ids) {
  return request({
    url: '/edu_chapter_exam_paper/batch',
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
