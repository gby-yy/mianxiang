package com.edu.teaching.basic.controller;

import com.edu.teaching.basic.service.StudentChapterExamService;
import com.edu.teaching.basic.service.StudentCourseLearnService;
import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import com.edu.teaching.module.edu_student_exam_answer.service.EduStudentExamAnswerService;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_exam_record.service.EduStudentExamRecordService;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生端章节测试：获取试卷、交卷阅卷、解锁下一章
 */
@RestController
@RequestMapping("/student")
public class StudentExamController {

    @Autowired
    private StudentChapterExamService studentChapterExamService;
    @Autowired
    private StudentCourseLearnService studentCourseLearnService;
    @Autowired
    private EduStudentExamRecordService eduStudentExamRecordService;
    @Autowired
    private EduStudentExamAnswerService eduStudentExamAnswerService;
    @Autowired
    private JwtUtil<StudentAuthController.LoginUser> jwtUtil;

    /**
     * 根据试卷ID获取试卷详情（含题目与选项），用于考试页
     */
    @GetMapping("/exam/paper/{paperId}")
    public Result getPaperDetail(@PathVariable Long paperId, HttpServletRequest request) {
        if (StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil) == null) {
            return Result.error(401, "尚未登录");
        }
        Map<String, Object> data = studentChapterExamService.getPaperDetail(paperId);
        if (data == null) {
            return Result.error(404, "试卷不存在");
        }
        return Result.success(data);
    }

    /**
     * 按章节与难度获取试卷详情（含题目与选项），无试卷时返回失败
     */
    @GetMapping("/exam/chapter-paper")
    public Result getChapterPaper(
        @RequestParam Long chapterId,
        @RequestParam Integer difficultyLevel,
        HttpServletRequest request) {
        if (StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil) == null) {
            return Result.error(401, "尚未登录");
        }
        Map<String, Object> data = studentChapterExamService.getChapterPaperByDifficulty(chapterId, difficultyLevel);
        if (data == null) {
            return Result.error(404, "该章节暂无对应难度的试卷，请选择其他难度或联系老师");
        }
        return Result.success(data);
    }

    /**
     * 提交章节测试并阅卷（含主观题AI判分），返回得分与是否及格
     */
    @PostMapping("/exam/submit")
    public Result submitExam(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long studentId = StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        Object paperIdObj = body.get("paperId");
        Object useTimeSecObj = body.get("useTimeSec");
        Object answersObj = body.get("answers");
        if (paperIdObj == null) {
            return Result.error(400, "缺少 paperId");
        }
        Long paperId = paperIdObj instanceof Number ? ((Number) paperIdObj).longValue() : Long.parseLong(String.valueOf(paperIdObj));
        int useTimeSec = useTimeSecObj != null ? ((Number) useTimeSecObj).intValue() : 0;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = answersObj instanceof List ? (List<Map<String, Object>>) answersObj : null;
        Map<String, Object> result = studentChapterExamService.submitChapterExam(studentId, paperId, useTimeSec, answers);
        return Result.success(result);
    }

    /**
     * 章节测试通过后解锁下一章
     */
    @PostMapping("/courses/{courseId}/unlock-next-chapter")
    public Result unlockNextChapter(
        @PathVariable Long courseId,
        @RequestBody Map<String, Object> body,
        HttpServletRequest request) {
        Long studentId = StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        Object afterObj = body != null ? body.get("afterChapterId") : null;
        if (afterObj == null) {
            return Result.error(400, "缺少 afterChapterId");
        }
        Long afterChapterId = afterObj instanceof Number ? ((Number) afterObj).longValue() : Long.parseLong(String.valueOf(afterObj));
        EduStudentCourseChapterRecord unlocked = studentCourseLearnService.unlockNextChapter(studentId, courseId, afterChapterId);
        if (unlocked == null) {
            return Result.success(null);
        }
        return Result.success(unlocked);
    }

    /**
     * 学生端：我的考试记录列表，支持课程、章节、试卷、题目关键词搜索
     */
    @GetMapping("/exam/records")
    public Result myExamRecords(
        @RequestParam(required = false) Long courseId,
        @RequestParam(required = false) Long chapterId,
        @RequestParam(required = false) String courseName,
        @RequestParam(required = false) String chapterName,
        @RequestParam(required = false) String paperName,
        @RequestParam(required = false) String questionKeyword,
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request) {
        Long studentId = StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        Map<String, Object> data = eduStudentExamRecordService.listByStudentWithFilter(
            studentId, courseId, chapterId, courseName, chapterName, paperName, questionKeyword, current, size);
        return Result.success(data);
    }

    /**
     * 学生端：某条考试记录的答题详情（仅本人可查）
     */
    @GetMapping("/exam/records/{recordId}/detail")
    public Result myExamRecordDetail(@PathVariable Long recordId, HttpServletRequest request) {
        Long studentId = StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        EduStudentExamRecord record = eduStudentExamRecordService.getById(recordId);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        if (!studentId.equals(record.getStudentId())) {
            return Result.error(403, "无权查看");
        }
        EduStudentExamAnswer query = new EduStudentExamAnswer();
        query.setExamRecordId(recordId);
        List<EduStudentExamAnswer> list = eduStudentExamAnswerService.listQuery(query);
        return Result.success(list);
    }
}
