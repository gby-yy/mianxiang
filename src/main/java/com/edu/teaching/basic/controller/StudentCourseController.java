package com.edu.teaching.basic.controller;

import cn.hutool.core.util.StrUtil;
import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.module.edu_course.service.EduCourseService;
import com.edu.teaching.module.edu_chapter_content.entity.EduChapterContent;
import com.edu.teaching.module.edu_chapter_content.service.EduChapterContentService;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.module.edu_question_bank.service.EduQuestionBankService;
import com.edu.teaching.module.edu_question_option.entity.EduQuestionOption;
import com.edu.teaching.module.edu_question_option.service.EduQuestionOptionService;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_exam_record.service.EduStudentExamRecordService;
import com.edu.teaching.basic.service.StudentCourseLearnService;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生端课程列表（仅返回「审核通过」且「状态启用」的课程）及选课/学习入口
 */
@RestController
@RequestMapping("/student")
public class StudentCourseController {

    /** 审核状态：1=通过（与前端 auditStatusEnumMap 一致） */
    private static final int AUDIT_STATUS_PASSED = 1;
    /** 课程状态：1=启用（与前端 statusEnumMap 一致） */
    private static final int COURSE_STATUS_ENABLED = 1;

    @Autowired
    private EduCourseService eduCourseService;
    @Autowired
    private StudentCourseLearnService studentCourseLearnService;
    @Autowired
    private EduChapterContentService eduChapterContentService;
    @Autowired
    private EduQuestionBankService eduQuestionBankService;
    @Autowired
    private EduQuestionOptionService eduQuestionOptionService;
    @Autowired
    private JwtUtil<StudentAuthController.LoginUser> jwtUtil;
    @Autowired
    private EduStudentExamRecordService eduStudentExamRecordService;

    /**
     * 分页查询学生可见课程列表
     * 仅展示：auditStatus=通过(1) 且 status=启用(1)
     */
    @GetMapping("/courses")
    public Result pageCourses(
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "8") Integer size,
        @RequestParam(required = false) String courseName,
        @RequestParam(required = false) Integer difficultyLevel) {
        EduCourse entity = new EduCourse();
        entity.setAuditStatus(AUDIT_STATUS_PASSED);
        entity.setStatus(COURSE_STATUS_ENABLED);
        if (courseName != null && !courseName.trim().isEmpty()) {
            entity.setCourseName(courseName.trim());
        }
        if (difficultyLevel != null) {
            entity.setDifficultyLevel(difficultyLevel);
        }
        return Result.success(eduCourseService.pageQuery(current, size, entity));
    }

    /**
     * 学生首页看板：已选课程的学习进度（已解锁/总章节、封面）与待办，以及基础统计（学习课程数、完成章节数、考试数、最高分）
     */
    @GetMapping("/dashboard")
    public Result getDashboard(HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        List<Map<String, Object>> list = studentCourseLearnService.getDashboardOverview(studentId);

        int studyCourseCount = list.size();
        int finishedChapterCount = 0;
        for (Map<String, Object> row : list) {
            Object v = row.get("finishedChapterCount");
            if (v instanceof Number) {
                finishedChapterCount += ((Number) v).intValue();
            }
        }

        EduStudentExamRecord examQuery = new EduStudentExamRecord();
        examQuery.setStudentId(studentId);
        List<EduStudentExamRecord> examRecords = eduStudentExamRecordService.listQuery(examQuery);
        int examCount = examRecords == null ? 0 : examRecords.size();
        BigDecimal maxScore = null;
        if (examRecords != null) {
            for (EduStudentExamRecord r : examRecords) {
                if (r.getTotalScore() != null) {
                    if (maxScore == null || r.getTotalScore().compareTo(maxScore) > 0) {
                        maxScore = r.getTotalScore();
                    }
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        Map<String, Object> stats = new HashMap<>();
        stats.put("studyCourseCount", studyCourseCount);
        stats.put("finishedChapterCount", finishedChapterCount);
        stats.put("examCount", examCount);
        stats.put("maxScore", maxScore != null ? maxScore : 0);
        data.put("stats", stats);
        return Result.success(data);
    }

    /**
     * 获取学生学习某课程的记录与章节列表（用于进入学习页时判断是否已选课、展示章节）
     * 无记录时 hasRecord=false，前端弹框选「0基础/有基础」后调 startLearn
     */
    @GetMapping("/courses/{courseId}/learn-info")
    public Result getLearnInfo(@PathVariable Long courseId, HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        EduStudentCourseRecord record = studentCourseLearnService.getRecord(studentId, courseId);
        if (record == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("hasRecord", false);
            return Result.success(data);
        }
        List<EduStudentCourseChapterRecord> chapterRecords = studentCourseLearnService.listChapterRecords(record.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("hasRecord", true);
        data.put("record", record);
        data.put("chapterRecords", chapterRecords);
        return Result.success(data);
    }

    /**
     * 首次选课：创建学习记录、复制章节、按基础类型解锁
     * @param courseId 课程ID
     * @param body foundationType: 0=0基础（仅解锁第1章基础章节），1=有基础（解锁所有基础+第1章难度章节）
     */
    @PostMapping("/courses/{courseId}/start")
    public Result startLearn(@PathVariable Long courseId, @RequestBody(required = false) Map<String, Integer> body, HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        int foundationType = (body != null && body.get("foundationType") != null) ? body.get("foundationType") : 0;
        EduStudentCourseRecord record = studentCourseLearnService.startLearn(studentId, courseId, foundationType);
        List<EduStudentCourseChapterRecord> chapterRecords = studentCourseLearnService.listChapterRecords(record.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("record", record);
        data.put("chapterRecords", chapterRecords);
        return Result.success(data);
    }

    /**
     * 获取某章节下的学习内容列表（按 sort_order 升序），用于章节详情页依次展示文档/视频
     * chapterId 为来源章节 id（edu_course_chapter.id）
     */
    @GetMapping("/courses/{courseId}/chapters/{chapterId}/contents")
    public Result listChapterContents(@PathVariable Long courseId, @PathVariable Long chapterId) {
        EduChapterContent entity = new EduChapterContent();
        entity.setChapterId(chapterId);
        List<EduChapterContent> list = eduChapterContentService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 上报某条内容已学习完成，更新该课程的内容学习进度 JSON 并重新计算课程学习进度
     * 学习信息接口返回的 record 中含 contentProgress，前端可据此回显每条内容是否已学
     * @param body contentId（必填，edu_chapter_content.id）
     */
    @PostMapping("/courses/{courseId}/content-progress")
    public Result reportContentProgress(@PathVariable Long courseId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        Object contentIdObj = body != null ? body.get("contentId") : null;
        if (contentIdObj == null) {
            return Result.error(400, "缺少 contentId");
        }
        Long contentId = null;
        if (contentIdObj instanceof Number) {
            contentId = ((Number) contentIdObj).longValue();
        } else if (contentIdObj instanceof String) {
            try {
                contentId = Long.parseLong((String) contentIdObj);
            } catch (NumberFormatException e) {
                return Result.error(400, "contentId 格式错误");
            }
        }
        if (contentId == null || contentId <= 0) {
            return Result.error(400, "contentId 无效");
        }
        EduStudentCourseRecord record = studentCourseLearnService.reportContentLearned(studentId, courseId, contentId);
        if (record == null) {
            return Result.error(404, "未选该课程或记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 获取某课程某章节下的题目列表（用于刷题页），仅返回启用状态题目，带选项
     * 前端可据此做上一题/下一题
     */
    @GetMapping("/courses/{courseId}/chapters/{chapterId}/questions")
    public Result listChapterQuestions(@PathVariable Long courseId, @PathVariable Long chapterId) {
        EduQuestionBank query = new EduQuestionBank();
        query.setCourseId(courseId);
        query.setChapterId(chapterId);
        query.setStatus(1);
        List<EduQuestionBank> banks = eduQuestionBankService.listQuery(query);
        List<Map<String, Object>> list = new ArrayList<>();
        for (EduQuestionBank q : banks) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("questionTitle", q.getQuestionTitle());
            item.put("questionType", q.getQuestionType());
            item.put("difficultyLevel", q.getDifficultyLevel());
            item.put("standardAnswer", q.getAnswerContent());
            item.put("answerAnalysis", q.getAnalysisContent());
            EduQuestionOption optQuery = new EduQuestionOption();
            optQuery.setQuestionId(q.getId());
            List<EduQuestionOption> opts = eduQuestionOptionService.listQuery(optQuery);
            List<Map<String, Object>> options = opts.stream().map(o -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", o.getId());
                m.put("optionKey", o.getOptionLabel());
                m.put("optionContent", o.getOptionContent());
                return m;
            }).collect(Collectors.toList());
            item.put("options", options);
            list.add(item);
        }
        return Result.success(list);
    }

    private Long getStudentIdFromRequest(HttpServletRequest request) {
        return getStudentIdFromRequestStatic(request, jwtUtil);
    }

    /**
     * 从请求中解析当前学生 ID（供其他学生端 Controller 复用）
     */
    public static Long getStudentIdFromRequestStatic(HttpServletRequest request, JwtUtil<StudentAuthController.LoginUser> jwtUtil) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization) || !StudentAuthController.getStudentTokenCacheMap().containsKey(authorization)) {
            return null;
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return null;
        }
        StudentAuthController.LoginUser loginUser = jwtUtil.parseToken(authorization, StudentAuthController.LoginUser.class);
        return loginUser != null ? loginUser.getId() : null;
    }
}
