package com.edu.teaching.module.edu_chapter_content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_chapter_content.entity.EduChapterContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节学习内容Mapper接口
 */
@Mapper
public interface EduChapterContentMapper extends BaseMapper<EduChapterContent> {

    /**
     * 分页查询
     */
    List<EduChapterContent> pageQuery(@Param("entity") EduChapterContent entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduChapterContent entity);

    /**
     * 列表查询（不分页）
     */
    List<EduChapterContent> listQuery(@Param("entity") EduChapterContent entity);

    /**
     * 根据ID查询
     */
    EduChapterContent getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_course_chapter
     */
    @Select("select chapter_name as label, id as value from edu_course_chapter")
    List<SelectVO> listEduCourseChapter();

}
