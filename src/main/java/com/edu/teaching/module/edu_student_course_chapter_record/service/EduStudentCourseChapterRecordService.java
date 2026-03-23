package com.edu.teaching.module.edu_student_course_chapter_record.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;

import java.util.List;

/**
 * 学生课程章节学习记录 Service
 */
public interface EduStudentCourseChapterRecordService extends IService<EduStudentCourseChapterRecord> {

    IPage<EduStudentCourseChapterRecord> pageQuery(Integer current, Integer size, EduStudentCourseChapterRecord entity);

    List<EduStudentCourseChapterRecord> listQuery(EduStudentCourseChapterRecord entity);

    EduStudentCourseChapterRecord getById(Long id);
}
