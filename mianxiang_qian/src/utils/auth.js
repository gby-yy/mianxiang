import Cookies from 'js-cookie'

// 管理端 token
const TokenKey = 'intelligent_teaching_system'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

// 学生端 token，与管理端完全隔离
const StudentTokenKey = 'intelligent_teaching_system_student'

export function getStudentToken() {
  return Cookies.get(StudentTokenKey)
}

export function setStudentToken(token) {
  return Cookies.set(StudentTokenKey, token)
}

export function removeStudentToken() {
  return Cookies.remove(StudentTokenKey)
}
