package com.edu.teaching.module.edu_student_exam_record.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_exam_record.mapper.EduStudentExamRecordMapper;
import com.edu.teaching.module.edu_student_exam_record.service.EduStudentExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuming
 * @description: 学生考试记录Service实现类
 */
@Service
public class EduStudentExamRecordServiceImpl extends ServiceImpl<EduStudentExamRecordMapper, EduStudentExamRecord> implements EduStudentExamRecordService {

    @Autowired
    private EduStudentExamRecordMapper eduStudentExamRecordMapper;

    @Override
    public IPage<EduStudentExamRecord> pageQuery(Integer current, Integer size, EduStudentExamRecord entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduStudentExamRecord> records = eduStudentExamRecordMapper.pageQuery(entity, offset, limit);
        Long total = eduStudentExamRecordMapper.countQuery(entity);

        Page<EduStudentExamRecord> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduStudentExamRecord> listQuery(EduStudentExamRecord entity) {
        return eduStudentExamRecordMapper.listQuery(entity);
    }

    @Override
    public EduStudentExamRecord getById(Long id) {
        return eduStudentExamRecordMapper.getById(id);
    }

    /**
     * 下拉数据：edu_chapter_exam_paper
     */
    @Override
    public List<SelectVO> listEduChapterExamPaper() {
        return eduStudentExamRecordMapper.listEduChapterExamPaper();
    }

    /**
     * 下拉数据：edu_student_user
     */
    @Override
    public List<SelectVO> listEduStudentUser() {
        return eduStudentExamRecordMapper.listEduStudentUser();
    }

    @Override
    public Map<String, Object> listByStudentWithFilter(Long studentId, Long courseId, Long chapterId,
            String courseName, String chapterName, String paperName, String questionKeyword, int page, int size) {
        long offset = (long) (page - 1) * size;
        long limit = size;
        List<EduStudentExamRecord> records = eduStudentExamRecordMapper.listByStudentWithFilter(
            studentId, courseId, chapterId, courseName, chapterName, paperName, questionKeyword, offset, limit);
        Long total = eduStudentExamRecordMapper.countByStudentWithFilter(
            studentId, courseId, chapterId, courseName, chapterName, paperName, questionKeyword);
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total != null ? total : 0L);
        return result;
    }

    @Override
    public List<EduStudentExamRecord> listByCourseId(Long courseId) {
        return courseId == null ? new java.util.ArrayList<>() : eduStudentExamRecordMapper.listByCourseId(courseId);
    }

}
