<template>
  <div class="student-placeholder">
    <div class="placeholder-card">
      <i class="el-icon-warning-outline placeholder-icon" />
      <h2 class="placeholder-title">{{ pageTitle }}</h2>
      <p class="placeholder-desc">该功能正在开发中，敬请期待。</p>
      <el-button type="primary" @click="$router.push('/student/index')">返回首页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentPlaceholder',
  computed: {
    pageTitle() {
      return (this.$route.meta && this.$route.meta.title) || '功能开发中'
    }
  }
}
</script>

<style lang="scss" scoped>
.student-placeholder {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.placeholder-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 48px 56px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  max-width: 420px;
}

.placeholder-icon {
  font-size: 64px;
  color: #91caff;
  margin-bottom: 20px;
}

.placeholder-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 12px;
}

.placeholder-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 24px;
}
</style>
