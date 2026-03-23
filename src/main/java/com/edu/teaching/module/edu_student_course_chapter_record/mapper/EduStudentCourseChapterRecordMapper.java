package com.edu.teaching.module.edu_student_course_chapter_record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生课程章节学习记录 Mapper
 */
@Mapper
public interface EduStudentCourseChapterRecordMapper extends BaseMapper<EduStudentCourseChapterRecord> {

    List<EduStudentCourseChapterRecord> pageQuery(@Param("entity") EduStudentCourseChapterRecord entity,
                                                   @Param("offset") Long offset,
                                                   @Param("limit") Long limit);

    Long countQuery(@Param("entity") EduStudentCourseChapterRecord entity);

    List<EduStudentCourseChapterRecord> listQuery(@Param("entity") EduStudentCourseChapterRecord entity);

    EduStudentCourseChapterRecord getById(@Param("id") Long id);
}
