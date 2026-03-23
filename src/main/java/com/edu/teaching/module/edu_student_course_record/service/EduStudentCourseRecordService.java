package com.edu.teaching.module.edu_student_course_record.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;

import java.util.List;

/**
 * 学生课程学习记录 Service
 */
public interface EduStudentCourseRecordService extends IService<EduStudentCourseRecord> {

    IPage<EduStudentCourseRecord> pageQuery(Integer current, Integer size, EduStudentCourseRecord entity);

    List<EduStudentCourseRecord> listQuery(EduStudentCourseRecord entity);

    EduStudentCourseRecord getById(Long id);

    EduStudentCourseRecord getByStudentIdAndCourseId(Long studentId, Long courseId);
}
