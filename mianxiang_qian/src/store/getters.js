const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.name,
  role: state => state.user.role,
  id: state => state.user.id,
  dynamicRoutes: state => state.app.dynamicRoutes,
  // 学生端
  studentToken: state => state.student.token,
  studentId: state => state.student.id,
  studentName: state => state.student.name,
  studentAvatar: state => state.student.avatar
}
export default getters
