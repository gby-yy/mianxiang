package com.edu.teaching.module.edu_chapter_content.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_chapter_content.entity.EduChapterContent;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节学习内容Service接口
 */
public interface EduChapterContentService extends IService<EduChapterContent> {

    /**
     * 分页查询
     */
    IPage<EduChapterContent> pageQuery(Integer current, Integer size, EduChapterContent entity);

    /**
     * 列表查询（不分页）
     */
    List<EduChapterContent> listQuery(EduChapterContent entity);

    /**
     * 根据ID查询
     */
    EduChapterContent getById(Long id);

    /**
     * 下拉数据：edu_course_chapter
     */
    List<SelectVO> listEduCourseChapter();

}
