package com.edu.teaching.module.edu_student_course_record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生课程学习记录 Mapper
 */
@Mapper
public interface EduStudentCourseRecordMapper extends BaseMapper<EduStudentCourseRecord> {

    List<EduStudentCourseRecord> pageQuery(@Param("entity") EduStudentCourseRecord entity,
                                           @Param("offset") Long offset,
                                           @Param("limit") Long limit);

    Long countQuery(@Param("entity") EduStudentCourseRecord entity);

    List<EduStudentCourseRecord> listQuery(@Param("entity") EduStudentCourseRecord entity);

    EduStudentCourseRecord getById(@Param("id") Long id);

    EduStudentCourseRecord getByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
