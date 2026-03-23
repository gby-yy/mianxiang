package com.edu.teaching.module.edu_exam_paper_question.dto;

import lombok.Data;

import java.util.List;

/**
 * @author wuming
 * @description: AI生成试卷题目请求DTO
 */
@Data
public class AiGeneratePaperQuestionDTO {
    /** 试卷ID */
    private Long paperId;
    /** 试卷难度：1-低，2-中，3-高 */
    private Integer difficultyLevel;
    /** 需要生成的题目数量 */
    private Integer questionCount;
    /** 可用题目列表 */
    private List<AvailableQuestion> availableQuestions;

    @Data
    public static class AvailableQuestion {
        /** 题目ID */
        private Long id;
        /** 题目标题 */
        private String title;
        /** 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，5-主观题 */
        private Integer type;
        /** 难度等级：1-简单，2-中等，3-困难 */
        private Integer difficulty;
        /** 题目分值 */
        private Double score;
    }
}
