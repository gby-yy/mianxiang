package com.edu.teaching.module.edu_student_exam_record.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.vo.SelectVO;

import java.util.List;
import java.util.Map;

/**
 * @author wuming
 * @description: 学生考试记录Service接口
 */
public interface EduStudentExamRecordService extends IService<EduStudentExamRecord> {

    /**
     * 分页查询
     */
    IPage<EduStudentExamRecord> pageQuery(Integer current, Integer size, EduStudentExamRecord entity);

    /**
     * 列表查询（不分页）
     */
    List<EduStudentExamRecord> listQuery(EduStudentExamRecord entity);

    /**
     * 根据ID查询
     */
    EduStudentExamRecord getById(Long id);

    /**
     * 下拉数据：edu_chapter_exam_paper
     */
    List<SelectVO> listEduChapterExamPaper();

    /**
     * 下拉数据：edu_student_user
     */
    List<SelectVO> listEduStudentUser();

    /**
     * 学生端：按条件分页查询本人记录，返回 records 与 total
     */
    Map<String, Object> listByStudentWithFilter(Long studentId, Long courseId, Long chapterId,
        String courseName, String chapterName, String paperName, String questionKeyword, int page, int size);

    /**
     * 按课程ID查询该课程下所有考试记录（含章节、试卷总分，用于课程数据分析）
     */
    List<EduStudentExamRecord> listByCourseId(Long courseId);

}
