package com.edu.teaching.basic.service.impl;

import com.edu.teaching.basic.mapper.EchartsMapper;
import com.edu.teaching.basic.service.EchartsService;
import com.edu.teaching.vo.EchartsDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;

@Service
public class EchartsServiceImpl implements EchartsService {

    @Autowired
    private EchartsMapper echartsMapper;

    /**
     * 系统用户角色分布统计
     */
    @Override
    public List<EchartsDataVO> countUserByRoleType(Integer status) {
        return echartsMapper.countUserByRoleType(status);
    }

    /**
     * 课程难度分布统计
     */
    @Override
    public List<EchartsDataVO> countCourseByDifficulty(Date startTime, Date endTime, Integer auditStatus) {
        return echartsMapper.countCourseByDifficulty(startTime, endTime, auditStatus);
    }

    /**
     * 题目类型分布统计
     */
    @Override
    public List<EchartsDataVO> countQuestionByType(Integer difficultyLevel, Integer status) {
        return echartsMapper.countQuestionByType(difficultyLevel, status);
    }

    /**
     * 考试提交状态统计
     */
    @Override
    public List<EchartsDataVO> countExamRecordBySubmitStatus(Date startTime, Date endTime) {
        return echartsMapper.countExamRecordBySubmitStatus(startTime, endTime);
    }

    /**
     * 不同难度试卷平均分统计
     */
    @Override
    public List<EchartsDataVO> avgExamScoreByPaperDifficulty(Date startTime, Date endTime) {
        return echartsMapper.avgExamScoreByPaperDifficulty(startTime, endTime);
    }

    /**
     * 章节内容类型分布统计
     */
    @Override
    public List<EchartsDataVO> countContentByContentType(Long courseId) {
        return echartsMapper.countContentByContentType(courseId);
    }

    @Override
    public List<EchartsDataVO> countPaperByDifficulty() {
        return echartsMapper.countPaperByDifficulty();
    }

}
