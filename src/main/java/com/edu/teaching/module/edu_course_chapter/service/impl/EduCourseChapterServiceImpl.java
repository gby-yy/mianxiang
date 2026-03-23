package com.edu.teaching.module.edu_course_chapter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_course_chapter.entity.EduCourseChapter;
import com.edu.teaching.module.edu_course_chapter.mapper.EduCourseChapterMapper;
import com.edu.teaching.module.edu_course_chapter.service.EduCourseChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 课程章节Service实现类
 */
@Service
public class EduCourseChapterServiceImpl extends ServiceImpl<EduCourseChapterMapper, EduCourseChapter> implements EduCourseChapterService {

    @Autowired
    private EduCourseChapterMapper eduCourseChapterMapper;

    @Override
    public IPage<EduCourseChapter> pageQuery(Integer current, Integer size, EduCourseChapter entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduCourseChapter> records = eduCourseChapterMapper.pageQuery(entity, offset, limit);
        Long total = eduCourseChapterMapper.countQuery(entity);

        Page<EduCourseChapter> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduCourseChapter> listQuery(EduCourseChapter entity) {
        return eduCourseChapterMapper.listQuery(entity);
    }

    @Override
    public EduCourseChapter getById(Long id) {
        return eduCourseChapterMapper.getById(id);
    }

    /**
     * 下拉数据：edu_course
     */
    @Override
    public List<SelectVO> listEduCourse() {
        return eduCourseChapterMapper.listEduCourse();
    }

}
