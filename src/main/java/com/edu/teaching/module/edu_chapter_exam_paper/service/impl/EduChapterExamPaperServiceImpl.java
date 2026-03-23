package com.edu.teaching.module.edu_chapter_exam_paper.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_chapter_exam_paper.entity.EduChapterExamPaper;
import com.edu.teaching.module.edu_chapter_exam_paper.mapper.EduChapterExamPaperMapper;
import com.edu.teaching.module.edu_chapter_exam_paper.service.EduChapterExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节试卷Service实现类
 */
@Service
public class EduChapterExamPaperServiceImpl extends ServiceImpl<EduChapterExamPaperMapper, EduChapterExamPaper> implements EduChapterExamPaperService {

    @Autowired
    private EduChapterExamPaperMapper eduChapterExamPaperMapper;

    @Override
    public IPage<EduChapterExamPaper> pageQuery(Integer current, Integer size, EduChapterExamPaper entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduChapterExamPaper> records = eduChapterExamPaperMapper.pageQuery(entity, offset, limit);
        Long total = eduChapterExamPaperMapper.countQuery(entity);

        Page<EduChapterExamPaper> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduChapterExamPaper> listQuery(EduChapterExamPaper entity) {
        return eduChapterExamPaperMapper.listQuery(entity);
    }

    @Override
    public EduChapterExamPaper getById(Long id) {
        return eduChapterExamPaperMapper.getById(id);
    }

    /**
     * 下拉数据：edu_course_chapter
     */
    @Override
    public List<SelectVO> listEduCourseChapter() {
        return eduChapterExamPaperMapper.listEduCourseChapter();
    }

}
