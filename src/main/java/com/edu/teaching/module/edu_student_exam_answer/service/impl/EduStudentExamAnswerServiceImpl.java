package com.edu.teaching.module.edu_student_exam_answer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import com.edu.teaching.module.edu_student_exam_answer.mapper.EduStudentExamAnswerMapper;
import com.edu.teaching.module.edu_student_exam_answer.service.EduStudentExamAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 学生考试记录详情Service实现类
 */
@Service
public class EduStudentExamAnswerServiceImpl extends ServiceImpl<EduStudentExamAnswerMapper, EduStudentExamAnswer> implements EduStudentExamAnswerService {

    @Autowired
    private EduStudentExamAnswerMapper eduStudentExamAnswerMapper;

    @Override
    public IPage<EduStudentExamAnswer> pageQuery(Integer current, Integer size, EduStudentExamAnswer entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduStudentExamAnswer> records = eduStudentExamAnswerMapper.pageQuery(entity, offset, limit);
        Long total = eduStudentExamAnswerMapper.countQuery(entity);

        Page<EduStudentExamAnswer> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduStudentExamAnswer> listQuery(EduStudentExamAnswer entity) {
        return eduStudentExamAnswerMapper.listQuery(entity);
    }

    @Override
    public EduStudentExamAnswer getById(Long id) {
        return eduStudentExamAnswerMapper.getById(id);
    }

    /**
     * 下拉数据：edu_student_exam_record
     */
    @Override
    public List<SelectVO> listEduStudentExamRecord() {
        return eduStudentExamAnswerMapper.listEduStudentExamRecord();
    }

    /**
     * 下拉数据：edu_question_bank
     */
    @Override
    public List<SelectVO> listEduQuestionBank() {
        return eduStudentExamAnswerMapper.listEduQuestionBank();
    }

}
