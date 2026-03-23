package com.edu.teaching.module.edu_student_course_record.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import com.edu.teaching.module.edu_student_course_record.mapper.EduStudentCourseRecordMapper;
import com.edu.teaching.module.edu_student_course_record.service.EduStudentCourseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生课程学习记录 Service 实现
 */
@Service
public class EduStudentCourseRecordServiceImpl extends ServiceImpl<EduStudentCourseRecordMapper, EduStudentCourseRecord>
        implements EduStudentCourseRecordService {

    @Autowired
    private EduStudentCourseRecordMapper eduStudentCourseRecordMapper;

    @Override
    public IPage<EduStudentCourseRecord> pageQuery(Integer current, Integer size, EduStudentCourseRecord entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduStudentCourseRecord> records = eduStudentCourseRecordMapper.pageQuery(entity, offset, limit);
        Long total = eduStudentCourseRecordMapper.countQuery(entity);
        Page<EduStudentCourseRecord> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduStudentCourseRecord> listQuery(EduStudentCourseRecord entity) {
        return eduStudentCourseRecordMapper.listQuery(entity);
    }

    @Override
    public EduStudentCourseRecord getById(Long id) {
        return eduStudentCourseRecordMapper.getById(id);
    }

    @Override
    public EduStudentCourseRecord getByStudentIdAndCourseId(Long studentId, Long courseId) {
        return eduStudentCourseRecordMapper.getByStudentIdAndCourseId(studentId, courseId);
    }
}
