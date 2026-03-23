package com.edu.teaching.module.edu_student_user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 学生Mapper接口
 */
@Mapper
public interface EduStudentUserMapper extends BaseMapper<EduStudentUser> {

    /**
     * 分页查询
     */
    List<EduStudentUser> pageQuery(@Param("entity") EduStudentUser entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduStudentUser entity);

    /**
     * 列表查询（不分页）
     */
    List<EduStudentUser> listQuery(@Param("entity") EduStudentUser entity);

    /**
     * 根据ID查询
     */
    EduStudentUser getById(@Param("id") Long id);

}
