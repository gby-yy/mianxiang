package com.edu.teaching.module.edu_question_option.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_question_option.entity.EduQuestionOption;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 题目选项Service接口
 */
public interface EduQuestionOptionService extends IService<EduQuestionOption> {

    /**
     * 分页查询
     */
    IPage<EduQuestionOption> pageQuery(Integer current, Integer size, EduQuestionOption entity);

    /**
     * 列表查询（不分页）
     */
    List<EduQuestionOption> listQuery(EduQuestionOption entity);

    /**
     * 根据ID查询
     */
    EduQuestionOption getById(Long id);

    /**
     * 下拉数据：edu_question_bank
     */
    List<SelectVO> listEduQuestionBank();

}
