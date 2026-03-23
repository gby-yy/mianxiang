package com.edu.teaching.module.edu_student_exam_record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 学生考试记录Mapper接口
 */
@Mapper
public interface EduStudentExamRecordMapper extends BaseMapper<EduStudentExamRecord> {

    /**
     * 分页查询
     */
    List<EduStudentExamRecord> pageQuery(@Param("entity") EduStudentExamRecord entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduStudentExamRecord entity);

    /**
     * 列表查询（不分页）
     */
    List<EduStudentExamRecord> listQuery(@Param("entity") EduStudentExamRecord entity);

    /**
     * 根据ID查询
     */
    EduStudentExamRecord getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_chapter_exam_paper
     */
    @Select("select paper_name as label, id as value from edu_chapter_exam_paper")
    List<SelectVO> listEduChapterExamPaper();

    /**
     * 下拉数据：edu_student_user
     */
    @Select("select real_name as label, id as value from edu_student_user")
    List<SelectVO> listEduStudentUser();

    /**
     * 学生端：按学生ID及可选条件分页查询（含课程、章节、试卷总分）
     */
    List<EduStudentExamRecord> listByStudentWithFilter(
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("chapterId") Long chapterId,
        @Param("courseName") String courseName,
        @Param("chapterName") String chapterName,
        @Param("paperName") String paperName,
        @Param("questionKeyword") String questionKeyword,
        @Param("offset") Long offset,
        @Param("limit") Long limit);

    Long countByStudentWithFilter(
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("chapterId") Long chapterId,
        @Param("courseName") String courseName,
        @Param("chapterName") String chapterName,
        @Param("paperName") String paperName,
        @Param("questionKeyword") String questionKeyword);

    /**
     * 按课程ID查询该课程下所有考试记录（含章节、试卷总分，用于课程数据分析）
     */
    List<EduStudentExamRecord> listByCourseId(@Param("courseId") Long courseId);

    /**
     * 全局：已提交考试次数与及格次数（用于管理员数据总览平均通过率）
     * 返回 Map: totalSubmitted (Long), passCount (Long)，及格线为试卷总分 60%
     */
    java.util.Map<String, Object> getSubmittedExamPassStats();

}
