package com.edu.teaching.basic.service;

import java.util.Map;

/**
 * 首页数据统计服务
 */
public interface DashboardService {

    /**
     * 根据角色获取首页统计数据：管理员为全局数据，教师为本人数据
     * @param role 角色：admin / teacher
     * @param userId 当前用户ID（教师时用于筛选）
     * @return 统计项 key-value，如 userCount, courseCount, examRecordCount, paperCount, questionCount
     */
    Map<String, Long> getStats(String role, Long userId);

    /**
     * 管理员数据总览：总学生数、总教师数、课程数量、学习总人次、平均通过率等
     * @return studentCount, teacherCount, courseCount, studySessionCount, avgPassRate（百分比，无则 0）
     */
    Map<String, Object> getAdminOverviewStats();
}
