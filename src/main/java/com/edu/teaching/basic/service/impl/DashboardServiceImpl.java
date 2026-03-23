package com.edu.teaching.basic.service.impl;

import com.edu.teaching.basic.service.DashboardService;
import com.edu.teaching.module.edu_chapter_exam_paper.entity.EduChapterExamPaper;
import com.edu.teaching.module.edu_chapter_exam_paper.mapper.EduChapterExamPaperMapper;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.module.edu_course.mapper.EduCourseMapper;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.module.edu_question_bank.mapper.EduQuestionBankMapper;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import com.edu.teaching.module.edu_student_course_record.mapper.EduStudentCourseRecordMapper;
import com.edu.teaching.module.edu_student_exam_record.mapper.EduStudentExamRecordMapper;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;
import com.edu.teaching.module.edu_student_user.mapper.EduStudentUserMapper;
import com.edu.teaching.module.edu_user.entity.EduUser;
import com.edu.teaching.module.edu_user.mapper.EduUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页数据统计服务实现：管理员全局统计，教师本人统计
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private EduUserMapper eduUserMapper;
    @Autowired
    private EduCourseMapper eduCourseMapper;
    @Autowired
    private EduStudentExamRecordMapper eduStudentExamRecordMapper;
    @Autowired
    private EduChapterExamPaperMapper eduChapterExamPaperMapper;
    @Autowired
    private EduQuestionBankMapper eduQuestionBankMapper;
    @Autowired
    private EduStudentUserMapper eduStudentUserMapper;
    @Autowired
    private EduStudentCourseRecordMapper eduStudentCourseRecordMapper;

    @Override
    public Map<String, Long> getStats(String role, Long userId) {
        Map<String, Long> stats = new HashMap<>();
        if (role == null) role = "";
        if ("1".equals(role)) role = "admin";
        if ("2".equals(role)) role = "teacher";
        if ("admin".equals(role)) {
            stats.put("userCount", eduUserMapper.countQuery(new EduUser()));
            stats.put("courseCount", eduCourseMapper.countQuery(new EduCourse()));
            stats.put("examRecordCount", eduStudentExamRecordMapper.countQuery(new EduStudentExamRecord()));
            stats.put("paperCount", eduChapterExamPaperMapper.countQuery(new EduChapterExamPaper()));
        } else {
            // 教师：仅统计本人数据
            EduCourse courseEntity = new EduCourse();
            courseEntity.setTeacherId(userId);
            EduChapterExamPaper paperEntity = new EduChapterExamPaper();
            paperEntity.setTeacherId(userId);
            EduQuestionBank questionEntity = new EduQuestionBank();
            questionEntity.setTeacherId(userId);
            stats.put("courseCount", eduCourseMapper.countQuery(courseEntity));
            stats.put("paperCount", eduChapterExamPaperMapper.countQuery(paperEntity));
            stats.put("questionCount", eduQuestionBankMapper.countQuery(questionEntity));
            stats.put("examRecordCount", 0L); // 教师端暂不统计考试记录数，可后续按试卷归属扩展
        }
        return stats;
    }

    @Override
    public Map<String, Object> getAdminOverviewStats() {
        Map<String, Object> map = new HashMap<>();
        map.put("studentCount", eduStudentUserMapper.countQuery(new EduStudentUser()));
        EduUser teacherQuery = new EduUser();
        teacherQuery.setRoleType("teacher");
        map.put("teacherCount", eduUserMapper.countQuery(teacherQuery));
        map.put("courseCount", eduCourseMapper.countQuery(new EduCourse()));
        map.put("studySessionCount", eduStudentCourseRecordMapper.countQuery(new EduStudentCourseRecord()));
        Map<String, Object> passStats = eduStudentExamRecordMapper.getSubmittedExamPassStats();
        if (passStats != null) {
            Object totalObj = passStats.get("totalSubmitted");
            Object passObj = passStats.get("passCount");
            long total = totalObj instanceof Number ? ((Number) totalObj).longValue() : 0L;
            long pass = passObj instanceof Number ? ((Number) passObj).longValue() : 0L;
            if (total > 0) {
                map.put("avgPassRate", BigDecimal.valueOf(pass * 100.0 / total).setScale(2, RoundingMode.HALF_UP));
            } else {
                map.put("avgPassRate", BigDecimal.ZERO);
            }
        } else {
            map.put("avgPassRate", BigDecimal.ZERO);
        }
        return map;
    }
}
