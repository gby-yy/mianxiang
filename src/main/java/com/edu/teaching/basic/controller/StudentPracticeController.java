package com.edu.teaching.basic.controller;

import com.edu.teaching.basic.service.SubjectiveScoreService;
import com.edu.teaching.common.Result;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生端刷题相关接口（如主观题 AI 判题）
 */
@RestController
@RequestMapping("/student")
public class StudentPracticeController {

    @Autowired
    private SubjectiveScoreService subjectiveScoreService;
    @Autowired
    private JwtUtil<StudentAuthController.LoginUser> jwtUtil;

    /**
     * 主观题 AI 判题：根据学生答案与标准答案调用 AI 评分并返回得分与判题理由
     * @param body questionId（可选）, questionTitle, userAnswer, standardAnswer（可选）
     * @return { score: 0-100, comment: "判题理由" }
     */
    @PostMapping("/practice/subjective-score")
    public Result submitSubjectiveScore(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil) == null) {
            return Result.error(401, "尚未登录");
        }
        String questionTitle = body != null && body.get("questionTitle") != null
            ? String.valueOf(body.get("questionTitle")) : "";
        String userAnswer = body != null && body.get("userAnswer") != null
            ? String.valueOf(body.get("userAnswer")) : "";
        String standardAnswer = body != null && body.get("standardAnswer") != null
            ? String.valueOf(body.get("standardAnswer")) : "";

        Map<String, Object> result = subjectiveScoreService.gradeSubjective(questionTitle, userAnswer, standardAnswer);
        return Result.success(result);
    }
}
