package com.edu.teaching.module.edu_exam_paper_question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_exam_paper_question.entity.EduExamPaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 试卷题目关联Mapper接口
 */
@Mapper
public interface EduExamPaperQuestionMapper extends BaseMapper<EduExamPaperQuestion> {

    /**
     * 分页查询
     */
    List<EduExamPaperQuestion> pageQuery(@Param("entity") EduExamPaperQuestion entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduExamPaperQuestion entity);

    /**
     * 列表查询（不分页）
     */
    List<EduExamPaperQuestion> listQuery(@Param("entity") EduExamPaperQuestion entity);

    /**
     * 根据ID查询
     */
    EduExamPaperQuestion getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_chapter_exam_paper
     */
    @Select("select paper_name as label, id as value from edu_chapter_exam_paper")
    List<SelectVO> listEduChapterExamPaper();

    /**
     * 下拉数据：edu_question_bank
     */
    @Select("select question_title as label, id as value from edu_question_bank")
    List<SelectVO> listEduQuestionBank();

}
