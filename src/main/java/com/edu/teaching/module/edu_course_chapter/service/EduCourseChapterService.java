package com.edu.teaching.module.edu_course_chapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_course_chapter.entity.EduCourseChapter;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 课程章节Service接口
 */
public interface EduCourseChapterService extends IService<EduCourseChapter> {

    /**
     * 分页查询
     */
    IPage<EduCourseChapter> pageQuery(Integer current, Integer size, EduCourseChapter entity);

    /**
     * 列表查询（不分页）
     */
    List<EduCourseChapter> listQuery(EduCourseChapter entity);

    /**
     * 根据ID查询
     */
    EduCourseChapter getById(Long id);

    /**
     * 下拉数据：edu_course
     */
    List<SelectVO> listEduCourse();

}
