package com.edu.teaching.module.edu_chapter_exam_paper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_chapter_exam_paper.entity.EduChapterExamPaper;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节试卷Service接口
 */
public interface EduChapterExamPaperService extends IService<EduChapterExamPaper> {

    /**
     * 分页查询
     */
    IPage<EduChapterExamPaper> pageQuery(Integer current, Integer size, EduChapterExamPaper entity);

    /**
     * 列表查询（不分页）
     */
    List<EduChapterExamPaper> listQuery(EduChapterExamPaper entity);

    /**
     * 根据ID查询
     */
    EduChapterExamPaper getById(Long id);

    /**
     * 下拉数据：edu_course_chapter
     */
    List<SelectVO> listEduCourseChapter();

}
