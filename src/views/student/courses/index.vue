<template>
  <div class="courses-page">
    <div class="page-header">
      <h1 class="page-title">课程列表</h1>
      <p class="page-desc">仅展示审核通过且已启用的课程，选择课程进行学习。</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-section">
      <el-form :inline="true" :model="query" class="search-form" size="medium">
        <el-form-item label="课程名称">
          <el-input
            v-model="query.courseName"
            placeholder="请输入课程名称"
            clearable
            style="width: 220px;"
            @keyup.enter.native="handleSearch"
          />
        </el-form-item>
        <el-form-item label="难度">
          <el-select
            v-model="query.difficultyLevel"
            placeholder="全部难度"
            clearable
            style="width: 140px;"
          >
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh-left" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 课程列表 -->
    <div v-loading="loading" class="course-list">
      <template v-if="courseList.length">
        <div
          v-for="(course, index) in courseList"
          :key="course.id"
          class="course-card"
          :class="'course-card--' + (index % 3)"
          @click="handleCourseClick(course)"
        >
          <div class="course-card-cover">
            <img v-if="course.cover" :src="course.cover" :alt="course.courseName">
            <div v-else class="cover-placeholder">
              <i class="el-icon-notebook-2" />
            </div>
            <div class="course-card-mask">
              <span class="go-text">进入课程</span>
              <i class="el-icon-arrow-right" />
            </div>
          </div>
          <div class="course-card-body">
            <h3 class="course-card-title">{{ course.courseName }}</h3>
            <el-tag
              :type="getDifficultyTagType(course.difficultyLevel)"
              size="small"
              class="course-card-tag"
            >
              {{ getDifficultyLabel(course.difficultyLevel) }}
            </el-tag>
            <p v-if="course.courseDesc" class="course-card-intro">{{ course.courseDesc }}</p>
            <p v-else class="course-card-intro no-intro">暂无简介</p>
            <p v-if="course.realName" class="course-card-teacher">
              <i class="el-icon-user" /> {{ course.realName }}
            </p>
          </div>
        </div>
      </template>
      <div v-else-if="!loading" class="empty-tip">
        <i class="el-icon-folder-opened" />
        <p>暂无开放课程</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <pagination
        v-show="total > 0"
        :total="total"
        :current-page.sync="query.current"
        :page-size.sync="query.size"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchCourses"
        @current-change="fetchCourses"
      />
    </div>
  </div>
</template>

<script>
import { pageStudentCourses } from '@/api/student'
import Pagination from '@/components/Pagination'

// 学生端课程列表：接口仅返回 auditStatus=通过(1) 且 status=启用(1) 的课程
export default {
  name: 'StudentCourses',
  components: { Pagination },
  data() {
    return {
      query: {
        current: 1,
        size: 8,
        courseName: '',
        difficultyLevel: null
      },
      courseList: [],
      total: 0,
      loading: false
    }
  },
  created() {
    this.fetchCourses()
  },
  methods: {
    fetchCourses() {
      this.loading = true
      pageStudentCourses({
        current: this.query.current,
        size: this.query.size,
        courseName: this.query.courseName || undefined,
        difficultyLevel: this.query.difficultyLevel || undefined
      }).then(res => {
        const data = res.data || {}
        this.courseList = data.records || []
        this.total = data.total != null ? data.total : 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.query.current = 1
      this.fetchCourses()
    },
    resetQuery() {
      this.query.courseName = ''
      this.query.difficultyLevel = null
      this.query.current = 1
      this.fetchCourses()
    },
    getDifficultyTagType(level) {
      const map = { 1: 'success', 2: 'warning', 3: 'danger' }
      return map[level] || 'info'
    },
    getDifficultyLabel(level) {
      const map = { 1: '简单', 2: '中等', 3: '困难' }
      return map[level] || '未知'
    },
    handleCourseClick(course) {
      this.$router.push({
        path: `/student/courses/learn/${course.id}`,
        query: { courseName: course.courseName || '' }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.courses-page {
  padding-bottom: 32px;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 8px;
  }

  .page-desc {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
  }
}

.search-section {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .search-form ::v-deep .el-form-item {
    margin-bottom: 0;
  }
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  min-height: 120px;
}

.course-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);

    .course-card-mask {
      opacity: 1;
    }
  }

  .course-card-cover {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 75%;
    background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
    overflow: hidden;

    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
      color: #1677ff;
      font-size: 48px;
    }

    .course-card-mask {
      position: absolute;
      inset: 0;
      background: rgba(22, 119, 255, 0.75);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #fff;
      font-size: 16px;
      font-weight: 600;
      opacity: 0;
      transition: opacity 0.25s ease;

      .el-icon-arrow-right {
        font-size: 18px;
      }
    }
  }

  .course-card-body {
    padding: 18px 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .course-card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 10px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .course-card-tag {
    align-self: flex-start;
    margin-bottom: 10px;
  }

  .course-card-intro {
    font-size: 13px;
    color: #6b7280;
    margin: 0 0 8px;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;

    &.no-intro {
      color: #9ca3af;
    }
  }

  .course-card-teacher {
    font-size: 12px;
    color: #9ca3af;
    margin: 0;
    margin-top: auto;

    i {
      margin-right: 4px;
    }
  }
}

/* 卡片视觉微差异 */
.course-card--0 .course-card-cover,
.course-card--0 .cover-placeholder {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
}
.course-card--1 .course-card-cover,
.course-card--1 .cover-placeholder {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
}
.course-card--2 .course-card-cover,
.course-card--2 .cover-placeholder {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
}

.course-card--1 .cover-placeholder { color: #15803d; }
.course-card--2 .cover-placeholder { color: #6d28d9; }

.empty-tip {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;

  i {
    font-size: 56px;
    margin-bottom: 16px;
    display: block;
  }
  p {
    margin: 0;
    font-size: 14px;
  }
}

.pagination-wrap {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}
</style>
