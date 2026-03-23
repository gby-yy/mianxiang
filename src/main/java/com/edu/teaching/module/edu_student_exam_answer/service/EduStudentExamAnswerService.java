package com.edu.teaching.module.edu_student_exam_answer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 学生考试记录详情Service接口
 */
public interface EduStudentExamAnswerService extends IService<EduStudentExamAnswer> {

    /**
     * 分页查询
     */
    IPage<EduStudentExamAnswer> pageQuery(Integer current, Integer size, EduStudentExamAnswer entity);

    /**
     * 列表查询（不分页）
     */
    List<EduStudentExamAnswer> listQuery(EduStudentExamAnswer entity);

    /**
     * 根据ID查询
     */
    EduStudentExamAnswer getById(Long id);

    /**
     * 下拉数据：edu_student_exam_record
     */
    List<SelectVO> listEduStudentExamRecord();

    /**
     * 下拉数据：edu_question_bank
     */
    List<SelectVO> listEduQuestionBank();

}
