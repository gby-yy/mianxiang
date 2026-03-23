package com.edu.teaching.module.edu_exam_paper_question.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_exam_paper_question.entity.EduExamPaperQuestion;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 试卷题目关联Service接口
 */
public interface EduExamPaperQuestionService extends IService<EduExamPaperQuestion> {

    /**
     * 分页查询
     */
    IPage<EduExamPaperQuestion> pageQuery(Integer current, Integer size, EduExamPaperQuestion entity);

    /**
     * 列表查询（不分页）
     */
    List<EduExamPaperQuestion> listQuery(EduExamPaperQuestion entity);

    /**
     * 根据ID查询
     */
    EduExamPaperQuestion getById(Long id);

    /**
     * 下拉数据：edu_chapter_exam_paper
     */
    List<SelectVO> listEduChapterExamPaper();

    /**
     * 下拉数据：edu_question_bank
     */
    List<SelectVO> listEduQuestionBank();

    /**
     * AI生成试卷题目
     * @param paperId 试卷ID
     * @param difficultyLevel 试卷难度：1-低，2-中，3-高
     * @param questionCount 需要生成的题目数量
     * @param availableQuestions 可用题目列表
     * @return 选中的题目ID列表
     */
    List<Long> aiGeneratePaperQuestions(Long paperId, Integer difficultyLevel, Integer questionCount, List<com.edu.teaching.module.edu_exam_paper_question.dto.AiGeneratePaperQuestionDTO.AvailableQuestion> availableQuestions);

}
