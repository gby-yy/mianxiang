package com.edu.teaching.module.edu_question_option.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_question_option.entity.EduQuestionOption;
import com.edu.teaching.module.edu_question_option.mapper.EduQuestionOptionMapper;
import com.edu.teaching.module.edu_question_option.service.EduQuestionOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 题目选项Service实现类
 */
@Service
public class EduQuestionOptionServiceImpl extends ServiceImpl<EduQuestionOptionMapper, EduQuestionOption> implements EduQuestionOptionService {

    @Autowired
    private EduQuestionOptionMapper eduQuestionOptionMapper;

    @Override
    public IPage<EduQuestionOption> pageQuery(Integer current, Integer size, EduQuestionOption entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduQuestionOption> records = eduQuestionOptionMapper.pageQuery(entity, offset, limit);
        Long total = eduQuestionOptionMapper.countQuery(entity);

        Page<EduQuestionOption> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduQuestionOption> listQuery(EduQuestionOption entity) {
        return eduQuestionOptionMapper.listQuery(entity);
    }

    @Override
    public EduQuestionOption getById(Long id) {
        return eduQuestionOptionMapper.getById(id);
    }

    /**
     * 下拉数据：edu_question_bank
     */
    @Override
    public List<SelectVO> listEduQuestionBank() {
        return eduQuestionOptionMapper.listEduQuestionBank();
    }

}
