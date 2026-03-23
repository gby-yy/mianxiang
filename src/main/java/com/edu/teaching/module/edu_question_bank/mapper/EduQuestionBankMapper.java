package com.edu.teaching.module.edu_question_bank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 章节题库Mapper接口
 */
@Mapper
public interface EduQuestionBankMapper extends BaseMapper<EduQuestionBank> {

    /**
     * 分页查询
     */
    List<EduQuestionBank> pageQuery(@Param("entity") EduQuestionBank entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduQuestionBank entity);

    /**
     * 列表查询（不分页）
     */
    List<EduQuestionBank> listQuery(@Param("entity") EduQuestionBank entity);

    /**
     * 根据ID查询
     */
    EduQuestionBank getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_course_chapter
     */
    @Select("select chapter_name as label, id as value from edu_course_chapter")
    List<SelectVO> listEduCourseChapter();

    /**
     * 下拉数据：edu_course
     */
    @Select("select course_name as label, id as value from edu_course")
    List<SelectVO> listEduCourse();

}
