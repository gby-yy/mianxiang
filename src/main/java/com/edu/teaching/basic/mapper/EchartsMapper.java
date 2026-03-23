package com.edu.teaching.basic.mapper;

import com.edu.teaching.vo.EchartsDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Date;

@Mapper
public interface EchartsMapper {

    /**
     * 系统用户角色分布统计
     */
    List<EchartsDataVO> countUserByRoleType(@Param("status") Integer status);

    /**
     * 课程难度分布统计
     */
    List<EchartsDataVO> countCourseByDifficulty(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("auditStatus") Integer auditStatus);

    /**
     * 题目类型分布统计
     */
    List<EchartsDataVO> countQuestionByType(@Param("difficultyLevel") Integer difficultyLevel, @Param("status") Integer status);

    /**
     * 考试提交状态统计
     */
    List<EchartsDataVO> countExamRecordBySubmitStatus(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 不同难度试卷平均分统计
     */
    List<EchartsDataVO> avgExamScoreByPaperDifficulty(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 章节内容类型分布统计
     */
    List<EchartsDataVO> countContentByContentType(@Param("courseId") Long courseId);

    /**
     * 试卷难度分布统计（全平台：简单/中等/困难）
     */
    List<EchartsDataVO> countPaperByDifficulty();

}
