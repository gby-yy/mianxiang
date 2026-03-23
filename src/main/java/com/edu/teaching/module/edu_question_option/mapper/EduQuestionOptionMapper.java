package com.edu.teaching.module.edu_question_option.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_question_option.entity.EduQuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 题目选项Mapper接口
 */
@Mapper
public interface EduQuestionOptionMapper extends BaseMapper<EduQuestionOption> {

    /**
     * 分页查询
     */
    List<EduQuestionOption> pageQuery(@Param("entity") EduQuestionOption entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduQuestionOption entity);

    /**
     * 列表查询（不分页）
     */
    List<EduQuestionOption> listQuery(@Param("entity") EduQuestionOption entity);

    /**
     * 根据ID查询
     */
    EduQuestionOption getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_question_bank
     */
    @Select("select question_title as label, id as value from edu_question_bank")
    List<SelectVO> listEduQuestionBank();

}
