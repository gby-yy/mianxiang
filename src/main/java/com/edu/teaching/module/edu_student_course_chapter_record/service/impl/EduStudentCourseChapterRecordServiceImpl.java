package com.edu.teaching.module.edu_student_course_chapter_record.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;
import com.edu.teaching.module.edu_student_course_chapter_record.mapper.EduStudentCourseChapterRecordMapper;
import com.edu.teaching.module.edu_student_course_chapter_record.service.EduStudentCourseChapterRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生课程章节学习记录 Service 实现
 */
@Service
public class EduStudentCourseChapterRecordServiceImpl extends ServiceImpl<EduStudentCourseChapterRecordMapper, EduStudentCourseChapterRecord>
        implements EduStudentCourseChapterRecordService {

    @Autowired
    private EduStudentCourseChapterRecordMapper eduStudentCourseChapterRecordMapper;

    @Override
    public IPage<EduStudentCourseChapterRecord> pageQuery(Integer current, Integer size, EduStudentCourseChapterRecord entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduStudentCourseChapterRecord> records = eduStudentCourseChapterRecordMapper.pageQuery(entity, offset, limit);
        Long total = eduStudentCourseChapterRecordMapper.countQuery(entity);
        Page<EduStudentCourseChapterRecord> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduStudentCourseChapterRecord> listQuery(EduStudentCourseChapterRecord entity) {
        return eduStudentCourseChapterRecordMapper.listQuery(entity);
    }

    @Override
    public EduStudentCourseChapterRecord getById(Long id) {
        return eduStudentCourseChapterRecordMapper.getById(id);
    }
}
