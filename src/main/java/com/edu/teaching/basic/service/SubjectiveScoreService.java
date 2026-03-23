package com.edu.teaching.basic.service;

import java.util.Map;

/**
 * 主观题 AI 判题：根据题目、标准答案与学生答案调用 AI 给出得分与判题理由
 */
public interface SubjectiveScoreService {

    /**
     * 对主观题作答进行 AI 评分
     * @param questionTitle 题目标题
     * @param userAnswer    学生答案
     * @param standardAnswer 标准答案（可为空）
     * @return 含 score(0-100)、comment(判题理由) 的 Map，AI 调用失败时 score 为 0、comment 为错误提示
     */
    Map<String, Object> gradeSubjective(String questionTitle, String userAnswer, String standardAnswer);
}
