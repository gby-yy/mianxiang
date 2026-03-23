package com.edu.teaching.module.edu_question_bank.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节题库Service接口
 */
public interface EduQuestionBankService extends IService<EduQuestionBank> {

    /**
     * 分页查询
     */
    IPage<EduQuestionBank> pageQuery(Integer current, Integer size, EduQuestionBank entity);

    /**
     * 列表查询（不分页）
     */
    List<EduQuestionBank> listQuery(EduQuestionBank entity);

    /**
     * 根据ID查询
     */
    EduQuestionBank getById(Long id);

    /**
     * 下拉数据：edu_course_chapter
     */
    List<SelectVO> listEduCourseChapter();

    /**
     * 下拉数据：edu_course
     */
    List<SelectVO> listEduCourse();

}
