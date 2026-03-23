package com.edu.teaching.basic.service.impl;

import com.edu.teaching.basic.service.SubjectiveScoreService;
import com.edu.teaching.util.AiChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主观题 AI 判题：调用大模型对作答评分并解析得分与理由
 */
@Service
@Slf4j
public class SubjectiveScoreServiceImpl implements SubjectiveScoreService {

    private static final String SYSTEM_PROMPT = "你是一名严谨的阅卷教师。请根据题目标题、标准答案对学生作答进行评分。"
        + "评分范围 0-100 分，综合考虑答案完整性、正确性、表述是否清晰。"
        + "你必须严格按以下格式输出两行，不要有多余内容：\n"
        + "分数：【0-100的整数】\n"
        + "理由：【一句话判题理由】";

    private static final Pattern SCORE_PATTERN = Pattern.compile("分数[：:]\s*([0-9]{1,3})");
    private static final Pattern REASON_PATTERN = Pattern.compile("理由[：:]\s*(.+)", Pattern.DOTALL);

    @Autowired
    private AiChatClient aiChatClient;

    @Override
    public Map<String, Object> gradeSubjective(String questionTitle, String userAnswer, String standardAnswer) {
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0);
        result.put("comment", "");

        if (userAnswer == null) {
            userAnswer = "";
        }
        userAnswer = userAnswer.trim();
        if (userAnswer.isEmpty()) {
            result.put("comment", "未作答");
            return result;
        }

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("题目：").append(questionTitle != null ? questionTitle : "").append("\n\n");
        if (standardAnswer != null && !standardAnswer.trim().isEmpty()) {
            userPrompt.append("标准答案：").append(standardAnswer.trim()).append("\n\n");
        }
        userPrompt.append("学生答案：").append(userAnswer).append("\n\n");
        userPrompt.append("请按要求的格式输出分数和理由。");

        try {
            String aiResponse = aiChatClient.chat(SYSTEM_PROMPT, userPrompt.toString());
            if (aiResponse == null || aiResponse.isEmpty()) {
                result.put("comment", "AI 评分服务暂不可用，请稍后重试");
                return result;
            }
            aiResponse = aiResponse.trim();
            int score = parseScore(aiResponse);
            String comment = parseReason(aiResponse);
            result.put("score", score);
            result.put("comment", comment != null ? comment.trim() : "暂无评语");
        } catch (Exception e) {
            log.error("主观题 AI 判题异常", e);
            result.put("comment", "评分失败：" + (e.getMessage() != null ? e.getMessage() : "请稍后重试"));
        }
        return result;
    }

    private int parseScore(String text) {
        Matcher m = SCORE_PATTERN.matcher(text);
        if (m.find()) {
            try {
                int s = Integer.parseInt(m.group(1).trim());
                return Math.min(100, Math.max(0, s));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private String parseReason(String text) {
        Matcher m = REASON_PATTERN.matcher(text);
        if (m.find()) {
            return m.group(1).trim().replaceAll("\\s+", " ");
        }
        return null;
    }
}
