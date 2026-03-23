package com.edu.teaching.module.edu_chapter_exam_paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_chapter_exam_paper.entity.EduChapterExamPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节试卷Mapper接口
 */
@Mapper
public interface EduChapterExamPaperMapper extends BaseMapper<EduChapterExamPaper> {

    /**
     * 分页查询
     */
    List<EduChapterExamPaper> pageQuery(@Param("entity") EduChapterExamPaper entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduChapterExamPaper entity);

    /**
     * 列表查询（不分页）
     */
    List<EduChapterExamPaper> listQuery(@Param("entity") EduChapterExamPaper entity);

    /**
     * 根据ID查询
     */
    EduChapterExamPaper getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_course_chapter
     */
    @Select("select chapter_name as label, id as value from edu_course_chapter")
    List<SelectVO> listEduCourseChapter();

}
