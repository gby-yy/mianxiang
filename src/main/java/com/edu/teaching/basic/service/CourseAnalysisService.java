package com.edu.teaching.basic.service;

import com.edu.teaching.module.edu_course.entity.EduCourse;

import java.util.List;
import java.util.Map;

/**
 * 课程数据分析：教师/管理员查看自己（或全部）课程的统计
 */
public interface CourseAnalysisService {

    /**
     * 获取当前用户可见的课程列表（教师仅本人课程，管理员全部）
     */
    List<EduCourse> getMyCourses(Long userId, String role);

    /**
     * 获取某课程的统计数据（需校验权限）
     * 返回：studentCount, totalExamCount, overallPassRate, chapterStats[], studentProgress[]
     */
    Map<String, Object> getCourseStats(Long courseId, Long userId, String role);

    /**
     * 将课程统计数据组装为面向 AI 的学情分析提示文本（用于流式分析）
     * 若无有效数据返回 null
     */
    String buildLearningAnalysisPrompt(Long courseId, Long userId, String role);
}
