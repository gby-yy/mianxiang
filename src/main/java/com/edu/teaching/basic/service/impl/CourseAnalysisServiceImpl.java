package com.edu.teaching.basic.service.impl;

import com.edu.teaching.basic.service.CourseAnalysisService;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.module.edu_course.service.EduCourseService;
import com.edu.teaching.module.edu_course_chapter.entity.EduCourseChapter;
import com.edu.teaching.module.edu_course_chapter.service.EduCourseChapterService;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;
import com.edu.teaching.module.edu_student_course_chapter_record.service.EduStudentCourseChapterRecordService;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import com.edu.teaching.module.edu_student_course_record.service.EduStudentCourseRecordService;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_exam_record.service.EduStudentExamRecordService;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;
import com.edu.teaching.module.edu_student_user.service.EduStudentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程数据分析实现：按课程统计学习进度、考试次数、各章节成绩与通过率
 */
@Service
public class CourseAnalysisServiceImpl implements CourseAnalysisService {

    private static final String ROLE_ADMIN = "admin";
    /** 及格线：试卷总分的 60% */
    private static final BigDecimal PASS_RATIO = new BigDecimal("0.60");

    @Autowired
    private EduCourseService eduCourseService;
    @Autowired
    private EduStudentCourseRecordService eduStudentCourseRecordService;
    @Autowired
    private EduStudentExamRecordService eduStudentExamRecordService;
    @Autowired
    private EduCourseChapterService eduCourseChapterService;
    @Autowired
    private EduStudentCourseChapterRecordService eduStudentCourseChapterRecordService;
    @Autowired
    private EduStudentUserService eduStudentUserService;

    @Override
    public List<EduCourse> getMyCourses(Long userId, String role) {
        EduCourse query = new EduCourse();
        if (userId != null && !ROLE_ADMIN.equalsIgnoreCase(role != null ? role : "")) {
            query.setTeacherId(userId);
        }
        List<EduCourse> list = eduCourseService.listQuery(query);
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public Map<String, Object> getCourseStats(Long courseId, Long userId, String role) {
        Map<String, Object> result = new LinkedHashMap<>();
        EduCourse course = eduCourseService.getById(courseId);
        if (course == null) {
            result.put("error", "课程不存在");
            return result;
        }
        boolean isAdmin = ROLE_ADMIN.equalsIgnoreCase(role != null ? role : "");
        if (!isAdmin && (userId == null || !userId.equals(course.getTeacherId()))) {
            result.put("error", "无权限查看该课程数据");
            return result;
        }

        // 选课学生数 & 学习进度
        EduStudentCourseRecord recordQuery = new EduStudentCourseRecord();
        recordQuery.setCourseId(courseId);
        List<EduStudentCourseRecord> courseRecords = eduStudentCourseRecordService.listQuery(recordQuery);
        int studentCount = courseRecords != null ? courseRecords.size() : 0;
        BigDecimal avgProgressRate = BigDecimal.ZERO;
        if (courseRecords != null && !courseRecords.isEmpty()) {
            BigDecimal sum = courseRecords.stream()
                .map(r -> r.getProgressRate() != null ? r.getProgressRate() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgProgressRate = sum.divide(BigDecimal.valueOf(courseRecords.size()), 2, RoundingMode.HALF_UP);
        }
        result.put("courseId", courseId);
        result.put("courseName", course.getCourseName());
        result.put("studentCount", studentCount);
        result.put("avgProgressRate", avgProgressRate);
        // 学生学习进度：已完成章节数按「内容学习 100%」实时统计，与学习页一致
        List<Map<String, Object>> progressList = new ArrayList<>();
        if (courseRecords != null) {
            for (EduStudentCourseRecord r : courseRecords) {
                EduStudentCourseChapterRecord chQuery = new EduStudentCourseChapterRecord();
                chQuery.setRecordId(r.getId());
                List<EduStudentCourseChapterRecord> chapterRecords = eduStudentCourseChapterRecordService.listQuery(chQuery);
                int finishedCount = 0;
                if (chapterRecords != null) {
                    for (EduStudentCourseChapterRecord ch : chapterRecords) {
                        if (ch.getStudyProgressRate() != null && ch.getStudyProgressRate().intValue() >= 100) {
                            finishedCount++;
                        }
                    }
                }
                int totalChapters = r.getChapterTotalCount() != null ? r.getChapterTotalCount() : (chapterRecords != null ? chapterRecords.size() : 0);
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("studentId", r.getStudentId());
                EduStudentUser student = r.getStudentId() != null ? eduStudentUserService.getById(r.getStudentId()) : null;
                p.put("realName", student != null ? student.getRealName() : null);
                p.put("avatar", student != null ? student.getAvatar() : null);
                p.put("username", student != null ? student.getUsername() : null);
                p.put("progressRate", r.getProgressRate() != null ? r.getProgressRate() : 0);
                p.put("finishedChapterCount", finishedCount);
                p.put("chapterTotalCount", totalChapters);
                progressList.add(p);
            }
        }
        result.put("studentProgress", progressList);

        // 该课程下所有考试记录（含章节、总分、试卷总分）
        List<EduStudentExamRecord> examRecords = eduStudentExamRecordService.listByCourseId(courseId);
        int totalExamCount = examRecords != null ? examRecords.size() : 0;
        result.put("totalExamCount", totalExamCount);

        int passCount = 0;
        BigDecimal totalScoreSum = BigDecimal.ZERO;
        int scoreCount = 0;
        if (examRecords != null) {
            for (EduStudentExamRecord r : examRecords) {
                if (r.getTotalScore() != null) {
                    totalScoreSum = totalScoreSum.add(r.getTotalScore());
                    scoreCount++;
                    BigDecimal paperTotal = r.getPaperTotalScore() != null && r.getPaperTotalScore().compareTo(BigDecimal.ZERO) > 0
                        ? r.getPaperTotalScore() : BigDecimal.valueOf(100);
                    if (r.getTotalScore().compareTo(paperTotal.multiply(PASS_RATIO)) >= 0) {
                        passCount++;
                    }
                }
            }
        }
        result.put("passCount", passCount);
        result.put("overallPassRate", totalExamCount > 0
            ? BigDecimal.valueOf(passCount * 100.0 / totalExamCount).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        result.put("avgScore", scoreCount > 0
            ? totalScoreSum.divide(BigDecimal.valueOf(scoreCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO);

        // 按章节聚合：章节通过率、平均分
        EduCourseChapter chapterQuery = new EduCourseChapter();
        chapterQuery.setCourseId(courseId);
        List<EduCourseChapter> chapters = eduCourseChapterService.listQuery(chapterQuery);
        List<Map<String, Object>> chapterStats = new ArrayList<>();
        if (chapters != null) {
            Map<Long, List<EduStudentExamRecord>> byChapter = examRecords != null
                ? examRecords.stream().filter(e -> e.getChapterId() != null).collect(Collectors.groupingBy(EduStudentExamRecord::getChapterId))
                : new HashMap<>();
            for (EduCourseChapter ch : chapters) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("chapterId", ch.getId());
                row.put("chapterName", ch.getChapterName());
                row.put("chapterOrder", ch.getChapterOrder());
                List<EduStudentExamRecord> list = byChapter.getOrDefault(ch.getId(), Collections.emptyList());
                int examCount = list.size();
                row.put("examCount", examCount);
                if (examCount == 0) {
                    row.put("avgScore", BigDecimal.ZERO);
                    row.put("passCount", 0);
                    row.put("passRate", BigDecimal.ZERO);
                } else {
                    BigDecimal sum = list.stream()
                        .map(r -> r.getTotalScore() != null ? r.getTotalScore() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    row.put("avgScore", sum.divide(BigDecimal.valueOf(examCount), 2, RoundingMode.HALF_UP));
                    long pass = list.stream().filter(r -> {
                        if (r.getTotalScore() == null) return false;
                        BigDecimal paperTotal = r.getPaperTotalScore() != null && r.getPaperTotalScore().compareTo(BigDecimal.ZERO) > 0
                            ? r.getPaperTotalScore() : BigDecimal.valueOf(100);
                        return r.getTotalScore().compareTo(paperTotal.multiply(PASS_RATIO)) >= 0;
                    }).count();
                    row.put("passCount", (int) pass);
                    row.put("passRate", BigDecimal.valueOf(pass * 100.0 / examCount).setScale(2, RoundingMode.HALF_UP));
                }
                chapterStats.add(row);
            }
            chapterStats.sort(Comparator.comparing(m -> (Integer) m.get("chapterOrder"), Comparator.nullsLast(Integer::compareTo)));
        }
        result.put("chapterStats", chapterStats);
        return result;
    }

    @Override
    public String buildLearningAnalysisPrompt(Long courseId, Long userId, String role) {
        Map<String, Object> stats = getCourseStats(courseId, userId, role);
        if (stats.containsKey("error")) return null;
        int studentCount = stats.get("studentCount") != null ? ((Number) stats.get("studentCount")).intValue() : 0;
        int totalExamCount = stats.get("totalExamCount") != null ? ((Number) stats.get("totalExamCount")).intValue() : 0;
        if (studentCount == 0 && totalExamCount == 0) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("【课程基本信息】\n");
        sb.append("课程名称：").append(stats.get("courseName")).append("\n");
        EduCourse course = eduCourseService.getById(courseId);
        if (course != null) {
            sb.append("章节数量：").append(course.getChapterCount() != null ? course.getChapterCount() : 0).append(" 章\n");
            sb.append("课程难度：").append(course.getDifficultyLevel() != null ? (course.getDifficultyLevel() == 1 ? "简单" : course.getDifficultyLevel() == 2 ? "中等" : "困难") : "未设置").append("\n");
        }
        sb.append("选课人数：").append(studentCount).append(" 人\n");
        sb.append("平均学习进度：").append(stats.get("avgProgressRate")).append("%\n");
        sb.append("考试总次数：").append(totalExamCount).append(" 次\n");
        sb.append("整体及格率：").append(stats.get("overallPassRate")).append("%\n");
        sb.append("平均分：").append(stats.get("avgScore")).append("\n\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapterStats = (List<Map<String, Object>>) stats.get("chapterStats");
        if (chapterStats != null && !chapterStats.isEmpty()) {
            sb.append("【各章节考试与通过情况】\n");
            for (Map<String, Object> row : chapterStats) {
                sb.append("- ").append(row.get("chapterName")).append("：考试 ").append(row.get("examCount")).append(" 次，平均分 ").append(row.get("avgScore")).append("，通过率 ").append(row.get("passRate")).append("%\n");
            }
            sb.append("\n");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> progressList = (List<Map<String, Object>>) stats.get("studentProgress");
        if (progressList != null && !progressList.isEmpty()) {
            sb.append("【每位学生学习情况】\n");
            for (Map<String, Object> p : progressList) {
                String name = p.get("realName") != null ? String.valueOf(p.get("realName")) : (p.get("username") != null ? String.valueOf(p.get("username")) : "学生");
                sb.append("- ").append(name).append("：已完成 ").append(p.get("finishedChapterCount")).append("/").append(p.get("chapterTotalCount")).append(" 章，学习进度 ").append(p.get("progressRate")).append("%\n");
            }
        }
        return sb.toString();
    }
}
