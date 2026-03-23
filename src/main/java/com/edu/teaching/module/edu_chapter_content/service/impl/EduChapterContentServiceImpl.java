package com.edu.teaching.module.edu_chapter_content.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_chapter_content.entity.EduChapterContent;
import com.edu.teaching.module.edu_chapter_content.mapper.EduChapterContentMapper;
import com.edu.teaching.module.edu_chapter_content.service.EduChapterContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节学习内容Service实现类
 */
@Service
public class EduChapterContentServiceImpl extends ServiceImpl<EduChapterContentMapper, EduChapterContent> implements EduChapterContentService {

    @Autowired
    private EduChapterContentMapper eduChapterContentMapper;

    @Override
    public IPage<EduChapterContent> pageQuery(Integer current, Integer size, EduChapterContent entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduChapterContent> records = eduChapterContentMapper.pageQuery(entity, offset, limit);
        Long total = eduChapterContentMapper.countQuery(entity);

        Page<EduChapterContent> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduChapterContent> listQuery(EduChapterContent entity) {
        return eduChapterContentMapper.listQuery(entity);
    }

    @Override
    public EduChapterContent getById(Long id) {
        return eduChapterContentMapper.getById(id);
    }

    /**
     * 下拉数据：edu_course_chapter
     */
    @Override
    public List<SelectVO> listEduCourseChapter() {
        return eduChapterContentMapper.listEduCourseChapter();
    }

}
