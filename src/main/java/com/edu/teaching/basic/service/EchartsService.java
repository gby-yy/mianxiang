package com.edu.teaching.basic.service;

import com.edu.teaching.vo.EchartsDataVO;

import java.util.List;
import java.util.Date;

public interface EchartsService {

    /**
     * 系统用户角色分布统计
     */
    List<EchartsDataVO> countUserByRoleType(Integer status);

    /**
     * 课程难度分布统计
     */
    List<EchartsDataVO> countCourseByDifficulty(Date startTime, Date endTime, Integer auditStatus);

    /**
     * 题目类型分布统计
     */
    List<EchartsDataVO> countQuestionByType(Integer difficultyLevel, Integer status);

    /**
     * 考试提交状态统计
     */
    List<EchartsDataVO> countExamRecordBySubmitStatus(Date startTime, Date endTime);

    /**
     * 不同难度试卷平均分统计
     */
    List<EchartsDataVO> avgExamScoreByPaperDifficulty(Date startTime, Date endTime);

    /**
     * 章节内容类型分布统计
     */
    List<EchartsDataVO> countContentByContentType(Long courseId);

    /**
     * 试卷难度分布统计（全平台）
     */
    List<EchartsDataVO> countPaperByDifficulty();

}
