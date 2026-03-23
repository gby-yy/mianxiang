package com.edu.teaching.module.edu_course_chapter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_course_chapter.entity.EduCourseChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 课程章节Mapper接口
 */
@Mapper
public interface EduCourseChapterMapper extends BaseMapper<EduCourseChapter> {

    /**
     * 分页查询
     */
    List<EduCourseChapter> pageQuery(@Param("entity") EduCourseChapter entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduCourseChapter entity);

    /**
     * 列表查询（不分页）
     */
    List<EduCourseChapter> listQuery(@Param("entity") EduCourseChapter entity);

    /**
     * 根据ID查询
     */
    EduCourseChapter getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_course
     */
    @Select("select course_name as label, id as value from edu_course")
    List<SelectVO> listEduCourse();

}
