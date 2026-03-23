package com.edu.teaching.basic.controller;

import com.edu.teaching.util.AiChatClient;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 学生端 AI 学习助手：流式对话
 */
@RestController
@RequestMapping("/student")
public class StudentAiChatController {

    @Autowired
    private AiChatClient aiChatClient;
    @Autowired
    private JwtUtil<StudentAuthController.LoginUser> jwtUtil;

    /**
     * 流式对话：POST 提交问题，返回 text/event-stream，前端逐 chunk 展示
     * body: userContent（必填）, systemContent（选填，默认学习助手角色）, chapterContext（选填，如章节名）
     */
    @PostMapping(value = "/ai-chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (StudentCourseController.getStudentIdFromRequestStatic(request, jwtUtil) == null) {
            throw new com.edu.teaching.common.BusinessException("尚未登录");
        }
        String userContent = body != null && body.get("userContent") != null
            ? String.valueOf(body.get("userContent")).trim() : "";
        if (userContent.isEmpty()) {
            throw new com.edu.teaching.common.BusinessException("请输入问题");
        }
        String systemContent = body != null && body.get("systemContent") != null
            ? String.valueOf(body.get("systemContent")) : null;
        String chapterContext = body != null && body.get("chapterContext") != null
            ? String.valueOf(body.get("chapterContext")) : null;
        if (systemContent == null || systemContent.isEmpty()) {
            systemContent = "你是一名耐心的课程学习助手，负责解答学生在学习过程中遇到的问题。回答请简洁清晰，适合学生理解。";
            if (chapterContext != null && !chapterContext.isEmpty()) {
                systemContent += "当前学生所在章节：" + chapterContext + "。回答时可结合该章节内容。";
            }
        }
        return aiChatClient.streamChat(systemContent, userContent);
    }
}
