package com.edu.teaching.module.edu_student_exam_answer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 学生考试记录详情Mapper接口
 */
@Mapper
public interface EduStudentExamAnswerMapper extends BaseMapper<EduStudentExamAnswer> {

    /**
     * 分页查询
     */
    List<EduStudentExamAnswer> pageQuery(@Param("entity") EduStudentExamAnswer entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduStudentExamAnswer entity);

    /**
     * 列表查询（不分页）
     */
    List<EduStudentExamAnswer> listQuery(@Param("entity") EduStudentExamAnswer entity);

    /**
     * 根据ID查询
     */
    EduStudentExamAnswer getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：edu_student_exam_record
     */
    @Select("select exam_no as label, id as value from edu_student_exam_record")
    List<SelectVO> listEduStudentExamRecord();

    /**
     * 下拉数据：edu_question_bank
     */
    @Select("select question_title as label, id as value from edu_question_bank")
    List<SelectVO> listEduQuestionBank();

}
