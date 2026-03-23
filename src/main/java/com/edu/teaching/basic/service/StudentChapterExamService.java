package com.edu.teaching.basic.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 学生章节测试：按难度获取试卷、提交阅卷
 */
public interface StudentChapterExamService {

    /**
     * 根据章节与难度获取试卷详情（含题目与选项），无则返回 null
     * @param chapterId 章节ID
     * @param difficultyLevel 1简单 2中等 3困难
     */
    Map<String, Object> getChapterPaperByDifficulty(Long chapterId, Integer difficultyLevel);

    /**
     * 根据试卷ID获取试卷详情（含题目与选项），用于考试页拉题
     */
    Map<String, Object> getPaperDetail(Long paperId);

    /**
     * 提交章节测试并阅卷，返回得分与是否及格
     * @param studentId 学生ID
     * @param paperId 试卷ID（edu_chapter_exam_paper.id）
     * @param useTimeSec 答题用时秒
     * @param answers 答案列表 [{ questionId, answer }]，多选为 "A,B,C"
     * @return { totalScore, paperTotalScore, passLine, passed, correctCount, wrongCount, recordId }
     */
    Map<String, Object> submitChapterExam(Long studentId, Long paperId, int useTimeSec, List<Map<String, Object>> answers);
}
