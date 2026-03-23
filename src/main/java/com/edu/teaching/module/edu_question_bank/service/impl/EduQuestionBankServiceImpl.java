package com.edu.teaching.module.edu_question_bank.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.module.edu_question_bank.mapper.EduQuestionBankMapper;
import com.edu.teaching.module.edu_question_bank.service.EduQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节题库Service实现类
 */
@Service
public class EduQuestionBankServiceImpl extends ServiceImpl<EduQuestionBankMapper, EduQuestionBank> implements EduQuestionBankService {

    @Autowired
    private EduQuestionBankMapper eduQuestionBankMapper;

    @Override
    public IPage<EduQuestionBank> pageQuery(Integer current, Integer size, EduQuestionBank entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduQuestionBank> records = eduQuestionBankMapper.pageQuery(entity, offset, limit);
        Long total = eduQuestionBankMapper.countQuery(entity);

        Page<EduQuestionBank> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduQuestionBank> listQuery(EduQuestionBank entity) {
        return eduQuestionBankMapper.listQuery(entity);
    }

    @Override
    public EduQuestionBank getById(Long id) {
        return eduQuestionBankMapper.getById(id);
    }

    /**
     * 下拉数据：edu_course_chapter
     */
    @Override
    public List<SelectVO> listEduCourseChapter() {
        return eduQuestionBankMapper.listEduCourseChapter();
    }

    /**
     * 下拉数据：edu_course
     */
    @Override
    public List<SelectVO> listEduCourse() {
        return eduQuestionBankMapper.listEduCourse();
    }

}
