package com.edu.teaching.module.edu_course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 课程Mapper接口
 */
@Mapper
public interface EduCourseMapper extends BaseMapper<EduCourse> {

    /**
     * 分页查询
     */
    List<EduCourse> pageQuery(@Param("entity") EduCourse entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduCourse entity);

    /**
     * 列表查询（不分页）
     */
    List<EduCourse> listQuery(@Param("entity") EduCourse entity);

    /**
     * 根据ID查询
     */
    EduCourse getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_user
     */
    @Select("select real_name as label, id as value from edu_user")
    List<SelectVO> listEduUser();

}
