import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* 布局组件 */
import Layout from '@/layout'

/**
 * 注意：子菜单仅在路由的 children 长度 >= 1 时才会显示
 *
 * hidden: true                   如果设置为 true，该项将不会显示在侧边栏中（默认为 false）
 * alwaysShow: true               如果设置为 true，将始终显示根菜单
 *                                如果未设置 alwaysShow，当该项有多个子路由时，
 *                                它将变为嵌套模式，否则不显示根菜单
 * redirect: noRedirect           如果设置为 noRedirect，面包屑导航中将不会重定向
 * name:'router-name'             该名称将被 <keep-alive> 使用（必须设置！！！）
 * meta : {
    roles: ['admin','editor']    控制页面的角色（可以设置多个角色）
    title: 'title'               显示在侧边栏和面包屑导航中的名称（建议设置）
    icon: 'svg-name'/'el-icon-x' 显示在侧边栏中的图标
    breadcrumb: false            如果设置为 false，该项将在面包屑导航中隐藏（默认为 true）
    activeMenu: '/example/list'  如果设置了路径，侧边栏将高亮显示您设置的路径
  }
 */

/**
 * 常量路由
 * 不需要权限的基础页面
 * 所有角色都可以访问
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },
  // 根路径：未登录或游客进入学生登录页；管理员登录后由 permission 重定向到 /index
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [{
      path: 'index',
      name: '首页',
      component: () => import('@/views/index/index'),
      meta: { title: '首页', icon: 'el-icon-s-home' }
    }]
  },
  // ========== 学生端路由：独立入口，不使用后台 Layout ==========
  {
    path: '/student/login',
    component: () => import('@/views/student/login/index'),
    hidden: true,
    meta: { title: '学生登录' }
  },
  {
    path: '/student/register',
    component: () => import('@/views/student/register/index'),
    hidden: true,
    meta: { title: '学生注册' }
  },
  {
    path: '/student',
    component: () => import('@/layout/StudentLayout'),
    redirect: '/student/index',
    hidden: true,
    meta: { title: '学生端' },
    children: [
      {
        path: 'index',
        name: 'StudentHome',
        component: () => import('@/views/student/index'),
        meta: { title: '首页' }
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('@/views/student/courses/index'),
        meta: { title: '课程列表' }
      },
      {
        path: 'courses/learn/:courseId',
        name: 'StudentCourseLearn',
        component: () => import('@/views/student/courses/learn'),
        meta: { title: '课程学习' }
      },
      {
        path: 'courses/learn/:courseId/chapter/:chapterId',
        name: 'StudentChapterDetail',
        component: () => import('@/views/student/courses/chapter-detail'),
        meta: { title: '章节学习' }
      },
      {
        path: 'practice',
        name: 'StudentPractice',
        component: () => import('@/views/student/_placeholder'),
        meta: { title: '刷题训练' }
      },
      {
        path: 'practice/do',
        name: 'StudentPracticeDo',
        component: () => import('@/views/student/practice/do'),
        meta: { title: '刷题' }
      },
      {
        path: 'exam',
        name: 'StudentExam',
        component: () => import('@/views/student/_placeholder'),
        meta: { title: '模拟考试' }
      },
      {
        path: 'exam/do',
        name: 'StudentExamDo',
        component: () => import('@/views/student/exam/do'),
        meta: { title: '章节测试' }
      },
      {
        path: 'exam/records',
        name: 'StudentExamRecords',
        component: () => import('@/views/student/exam/records'),
        meta: { title: '考试记录' }
      },
      {
        path: 'wrong',
        name: 'StudentWrong',
        component: () => import('@/views/student/_placeholder'),
        meta: { title: '错题库' }
      },

      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/profile/index'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

/**
 * 异步路由
 * 需要根据角色动态添加的路由
 */
export const asyncRoutes = [
  {
    path: "/user-manage",
    name: "userManage",
    component: Layout,
    meta: { title: "用户管理", icon: "el-icon-user-solid", roles: ["admin"] },
    redirect: "/user-manage/student",
    children: [
      { path: "/user-manage/student", name: "studentManage", component: () => import("@/views/user/student/index"), meta: { title: "学生管理", icon: "el-icon-user", roles: ["admin"] } },
      { path: "/user-manage/teacher", name: "teacherManage", component: () => import("@/views/user/teacher/index"), meta: { title: "教师管理", icon: "el-icon-s-custom", roles: ["admin"] } }
    ]
  },
  {
    path: "/course-manage",
    name: "courseManage",
    component: Layout,
    meta: { title: "课程管理", icon: "el-icon-reading", roles: ["admin", "teacher"] },
    redirect: "/course-manage/course-list",
    children: [
      { path: "/course-manage/course-list", name: "courseList", component: () => import("@/views/course/course-list/index"), meta: { title: "课程列表", icon: "el-icon-document", roles: ["admin", "teacher"] } },
      { path: "/course-manage/course-chapter", name: "courseChapter", component: () => import("@/views/course/course-chapter/index"), meta: { title: "课程详情", icon: "el-icon-menu", roles: ["teacher"] } }
    ]
  },
  {
    path: "/question-bank",
    name: "questionBank",
    component: Layout,
    meta: { title: "题库管理", icon: "el-icon-edit-outline", roles: ["admin", "teacher"] },
    redirect: "/question-bank/index",
    children: [
      { path: "/question-bank/index", name: "questionBankIndex", component: () => import("@/views/question-bank/index"), meta: { title: "题库管理", icon: "el-icon-edit", roles: ["admin", "teacher"] } }
    ]
  },
  {
    path: "/exam-manage",
    name: "examManage",
    component: Layout,
    meta: { title: "考试管理", icon: "el-icon-document-copy", roles: ["admin", "teacher"] },
    redirect: "/exam-manage/paper-list",
    children: [
      { path: "/exam-manage/paper-list", name: "paperList", component: () => import("@/views/exam/paper-list/index"), meta: { title: "试卷列表", icon: "el-icon-tickets", roles: ["admin", "teacher"] } },
      { path: "/exam-manage/exam-record", name: "examRecord", component: () => import("@/views/exam/exam-record/index"), meta: { title: "考试记录", icon: "el-icon-edit-outline", roles: ["admin", "teacher"] } }
    ]
  },
  {
    path: "/data-analysis",
    name: "dataAnalysis",
    component: Layout,
    meta: { title: "数据分析", icon: "el-icon-data-analysis", roles: ["admin", "teacher"] },
    redirect: "/data-analysis/overview",
    children: [
      { path: "/data-analysis/overview", name: "dataOverview", component: () => import("@/views/analysis/overview/index"), meta: { title: "数据总览", icon: "el-icon-s-data", roles: ["admin"] } },
      { path: "/data-analysis/course-analysis", name: "courseAnalysis", component: () => import("@/views/analysis/course/index"), meta: { title: "课程数据分析", icon: "el-icon-pie-chart", roles: ["teacher"] } },
      { path: "/data-analysis/ai-analysis", name: "aiAnalysis", component: () => import("@/views/analysis/ai/index"), meta: { title: "AI学情分析", icon: "el-icon-cpu", roles: ["teacher"] } }
    ]
  }
]

const createRouter = () => new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

/**
 * 根据角色添加路由
 * @param {Array} roles - 用户角色数组
 * @returns {Array} - 符合用户角色的可访问路由数组
 */
export function addRoutesByRoles(roles) {
  // 递归过滤子路由
  function filterChildRoutes(children) {
    if (!children) return [];
    return children.filter(child => {
      let hasAccess = false;
      // 检查当前路由是否有可访问的角色
      if (child.meta && child.meta.roles) {
        hasAccess = roles.some(role => child.meta.roles.includes(role));
      } else {
        hasAccess = true;
      }

      // 递归处理子路由
      if (child.children) {
        const filteredChildren = filterChildRoutes(child.children);
        child.children = filteredChildren;
        // 如果当前路由没有直接访问权限，但子路由有，则认为该路由可访问
        hasAccess = hasAccess || filteredChildren.length > 0;
      }
      return hasAccess;
    });
  }
  const accessedRoutes = asyncRoutes.filter(route => {
    const filteredChildren = filterChildRoutes(route.children);
    route.children = filteredChildren;
    // 如果父路由有子路由，或者父路由自身有匹配的角色，则保留该父路由
    const hasRouteAccess = filteredChildren.length > 0;
    return hasRouteAccess;
  });
  // 动态添加可访问的路由到路由实例中
  router.addRoutes(accessedRoutes);
  // 返回符合用户角色的可访问路由数组
  return accessedRoutes;
}

export default router
