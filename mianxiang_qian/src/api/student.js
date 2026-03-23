import request from '@/utils/request'
import { getStudentToken } from '@/utils/auth'

/**
 * 学生端登录与鉴权接口
 * 请求时会自动携带学生 token（见 utils/request.js 中对 /student 的拦截）
 */

export function studentLogin(data) {
  return request({
    url: '/student/login',
    method: 'post',
    data
  })
}

export function studentRegister(data) {
  return request({
    url: '/student/register',
    method: 'post',
    data
  })
}

export function studentGetInfo() {
  return request({
    url: '/student/info',
    method: 'get'
  })
}

/**
 * 学生个人中心：获取完整资料
 */
export function getStudentProfile() {
  return request({
    url: '/student/profile',
    method: 'get'
  })
}

/**
 * 学生个人中心：更新资料
 */
export function updateStudentProfile(data) {
  return request({
    url: '/student/profile',
    method: 'put',
    data: data || {}
  })
}

/**
 * 学生个人中心：修改密码（oldPassword/newPassword 为前端 SHA256 后的值，与登录一致）
 */
export function changeStudentPassword(data) {
  return request({
    url: '/student/password',
    method: 'post',
    data: data || {}
  })
}

export function studentLogout() {
  return request({
    url: '/student/logout',
    method: 'get'
  })
}

/**
 * 学生首页看板：已选课程的学习进度（已解锁/总章节）与待办（待学习、待测试）
 * @returns Array<{ courseId, courseName, recordId, unlockedChapterCount, totalChapterCount, finishedChapterCount, progressRate, todoUnlearned, todoUntested }>
 */
export function getStudentDashboard() {
  return request({
    url: '/student/dashboard',
    method: 'get'
  })
}

/**
 * 学生端：分页获取课程列表（仅已启用、已审核的课程）
 * @param params { current, size, courseName?, difficultyLevel? }
 * @returns { records, total, size, current }
 */
export function pageStudentCourses(params) {
  return request({
    url: '/student/courses',
    method: 'get',
    params: params || {}
  })
}

/**
 * 学生端：获取某课程学习信息（是否已有记录、记录与章节列表）
 * @param courseId 课程ID
 * @returns { hasRecord, record?, chapterRecords? }
 */
export function getCourseLearnInfo(courseId) {
  return request({
    url: `/student/courses/${courseId}/learn-info`,
    method: 'get'
  })
}

/**
 * 学生端：首次选课并开始学习（创建记录、复制章节、按基础类型解锁）
 * @param courseId 课程ID
 * @param foundationType 0=0基础（仅解锁第1章基础），1=有基础（解锁所有基础+第1章难度）
 * @returns { record, chapterRecords }
 */
export function startCourseLearn(courseId, foundationType) {
  return request({
    url: `/student/courses/${courseId}/start`,
    method: 'post',
    data: { foundationType: foundationType === 1 ? 1 : 0 }
  })
}

/**
 * 学生端：获取某章节下的学习内容列表（按排序依次展示文档/视频）
 * @param courseId 课程ID
 * @param chapterId 来源章节ID（edu_course_chapter.id）
 */
export function getChapterContents(courseId, chapterId) {
  return request({
    url: `/student/courses/${courseId}/chapters/${chapterId}/contents`,
    method: 'get'
  })
}

/**
 * 学生端：上报某条内容已学习完成，更新课程与章节进度
 * @param courseId 课程ID
 * @param data { contentId: number } 内容ID（edu_chapter_content.id）
 * @returns 更新后的学习记录（含 contentProgress、progressRate 等）
 */
export function reportContentProgress(courseId, data) {
  return request({
    url: `/student/courses/${courseId}/content-progress`,
    method: 'post',
    data: data || {}
  })
}

/**
 * 学生端：按章节与难度获取试卷（章节测试选难度后调用）
 * @param chapterId 章节ID
 * @param difficultyLevel 1简单 2中等 3困难
 */
export function getChapterPaper(chapterId, difficultyLevel) {
  return request({
    url: '/student/exam/chapter-paper',
    method: 'get',
    params: { chapterId, difficultyLevel }
  })
}

/**
 * 学生端：根据试卷ID获取试卷详情（考试页拉题）
 */
export function getExamPaperDetail(paperId) {
  return request({
    url: `/student/exam/paper/${paperId}`,
    method: 'get'
  })
}

/**
 * 学生端：提交章节测试（后端阅卷，含主观题AI）
 * @param data { paperId, useTimeSec, answers: [{ questionId, answer }] }
 * @returns { totalScore, paperTotalScore, passLine, passed, correctCount, wrongCount, recordId }
 */
export function submitChapterExam(data) {
  return request({
    url: '/student/exam/submit',
    method: 'post',
    data: data || {}
  })
}

/**
 * 学生端：章节测试通过后解锁下一章
 */
export function unlockNextChapter(courseId, afterChapterId) {
  return request({
    url: `/student/courses/${courseId}/unlock-next-chapter`,
    method: 'post',
    data: { afterChapterId }
  })
}

/**
 * 学生端：AI 学习助手流式对话
 * @param params { userContent, systemContent?, chapterContext? }
 * @param onChunk 每收到一段文本回调 (text: string) => void
 * @returns Promise<void> 流结束或失败时 resolve/reject
 */
export function streamAiChat(params, onChunk) {
  const baseURL = process.env.VUE_APP_BASE_API || ''
  const url = (baseURL.replace(/\/$/, '') + '/student/ai-chat/stream').replace(/([^:])\/\/+/, '$1/')
  const token = getStudentToken()
  return fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: token } : {})
    },
    body: JSON.stringify({
      userContent: params.userContent || '',
      systemContent: params.systemContent || undefined,
      chapterContext: params.chapterContext || undefined
    })
  }).then(response => {
    if (!response.ok) {
      return response.text().then(t => Promise.reject(new Error(t || response.statusText)))
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    function processBuffer() {
      const lines = buffer.split(/\r?\n/)
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.replace(/^data:\s?/, '').replace(/\r/g, '').trim()
          if (onChunk) onChunk(data)
        }
      }
    }
    function read() {
      return reader.read().then(({ done, value }) => {
        if (value) {
          buffer += decoder.decode(value, { stream: true })
          processBuffer()
        }
        if (done) {
          processBuffer()
          if (buffer.length && buffer.startsWith('data:') && onChunk) {
            const data = buffer.replace(/^data:\s?/, '').replace(/\r/g, '').trim()
            onChunk(data)
          }
          return
        }
        return read()
      })
    }
    return read()
  })
}

/**
 * 学生端：获取某课程某章节下的题目列表（用于刷题页），带选项
 * @param courseId 课程ID
 * @param chapterId 章节ID（edu_course_chapter.id）
 * @returns 题目列表，每项含 id, questionTitle, questionType, standardAnswer, answerAnalysis, options
 */
export function getChapterQuestions(courseId, chapterId) {
  return request({
    url: `/student/courses/${courseId}/chapters/${chapterId}/questions`,
    method: 'get'
  })
}

/**
 * 学生端：主观题 AI 判题
 * @param data { questionTitle, userAnswer, standardAnswer? }
 * @returns { score: 0-100, comment: string }
 */
export function submitSubjectiveScore(data) {
  return request({
    url: '/student/practice/subjective-score',
    method: 'post',
    data: data || {}
  })
}

/**
 * 学生端：我的考试记录列表（支持课程、章节、试卷、题目搜索）
 * @param params { current, size, courseId?, chapterId?, courseName?, chapterName?, paperName?, questionKeyword? }
 * @returns { records, total }
 */
export function getMyExamRecords(params) {
  return request({
    url: '/student/exam/records',
    method: 'get',
    params: params || {}
  })
}

/**
 * 学生端：某条考试记录的答题详情
 */
export function getMyExamRecordDetail(recordId) {
  return request({
    url: `/student/exam/records/${recordId}/detail`,
    method: 'get'
  })
}
