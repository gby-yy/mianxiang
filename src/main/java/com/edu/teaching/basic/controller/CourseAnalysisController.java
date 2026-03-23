package com.edu.teaching.basic.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.edu.teaching.common.Result;
import com.edu.teaching.basic.service.CourseAnalysisService;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.util.AiChatClient;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程数据分析：教师查看本人课程、管理员查看全部；按课程查看学习进度与考试统计
 */
@RestController
@RequestMapping("/analysis")
public class CourseAnalysisController {

    @Autowired
    private CourseAnalysisService courseAnalysisService;
    @Autowired
    private JwtUtil<EduUserLoginController.LoginUser> jwtUtil;
    @Autowired
    private AiChatClient aiChatClient;

    /**
     * 获取当前用户可见的课程列表（教师仅本人，管理员全部）
     */
    @GetMapping("/my-courses")
    public Result listMyCourses(HttpServletRequest request) {
        EduUserLoginController.LoginUser user = getLoginUser(request);
        if (user == null) {
            return Result.error(401, "未登录");
        }
        String role = user.getRole() != null ? user.getRole() : "";
        List<EduCourse> list = courseAnalysisService.getMyCourses(user.getId(), role);
        return Result.success(list);
    }

    /**
     * 获取某课程的详细统计数据：学生数、学习进度、考试次数、及格率、各章节通过率与平均分
     */
    @GetMapping("/course/{courseId}/stats")
    public Result getCourseStats(@PathVariable Long courseId, HttpServletRequest request) {
        EduUserLoginController.LoginUser user = getLoginUser(request);
        if (user == null) {
            return Result.error(401, "未登录");
        }
        String role = user.getRole() != null ? user.getRole() : "";
        Map<String, Object> stats = courseAnalysisService.getCourseStats(courseId, user.getId(), role);
        if (stats.containsKey("error")) {
            return Result.error(403, (String) stats.get("error"));
        }
        return Result.success(stats);
    }

    private static final String AI_SYSTEM_PROMPT = "你是一名教学分析助手，根据给定的课程学情数据，生成一份简洁、结构清晰的学情分析总结。要求：1）概括课程整体学习与考试情况；2）按章节指出掌握较好的与待加强的；3）对学生学习情况做简要归纳与改进建议。用中文回答，分点表述，语气专业但易读。";

    /**
     * AI 学情分析：按课程维度组装数据并流式返回 AI 分析结果；若无数据则返回 no_data 事件
     */
    @PostMapping(value = "/ai-learning/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAiLearningAnalysis(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        EduUserLoginController.LoginUser user = getLoginUser(request);
        if (user == null) {
            throw new com.edu.teaching.common.BusinessException("未登录");
        }
        Object courseIdObj = body != null ? body.get("courseId") : null;
        Long courseId = courseIdObj instanceof Number ? ((Number) courseIdObj).longValue() : null;
        if (courseId == null && courseIdObj instanceof String) {
            try {
                courseId = Long.parseLong((String) courseIdObj);
            } catch (NumberFormatException ignored) {}
        }
        if (courseId == null) {
            throw new com.edu.teaching.common.BusinessException("请选择课程");
        }
        String role = user.getRole() != null ? user.getRole() : "";
        String dataText = courseAnalysisService.buildLearningAnalysisPrompt(courseId, user.getId(), role);
        SseEmitter emitter = new SseEmitter(90_000L);
        if (dataText == null || dataText.isEmpty()) {
            try {
                Map<String, Object> noData = new HashMap<>();
                noData.put("type", "no_data");
                noData.put("message", "该课程暂无选课学生与考试记录，无法生成学情分析。");
                emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(noData)).name("no_data"));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            emitter.complete();
            return emitter;
        }
        String userPrompt = "以下是一门课程的学情数据，请基于此生成学情分析总结：\n\n" + dataText;
        aiChatClient.streamChatToEmitter(emitter, AI_SYSTEM_PROMPT, userPrompt);
        return emitter;
    }

    private EduUserLoginController.LoginUser getLoginUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization) || !jwtUtil.verifyToken(authorization)) {
            return null;
        }
        return jwtUtil.parseToken(authorization, EduUserLoginController.LoginUser.class);
    }
}
