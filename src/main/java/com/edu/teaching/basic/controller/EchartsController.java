package com.edu.teaching.basic.controller;

import com.edu.teaching.basic.service.EchartsService;
import com.edu.teaching.vo.EchartsDataVO;
import com.edu.teaching.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Date;
import com.alibaba.fastjson2.util.DateUtils;

/**
 * 图表可视化接口
 */
@RestController
@RequestMapping("/echarts")
public class EchartsController {

    @Autowired
    private EchartsService echartsService;

    /**
     * 系统用户角色分布统计
     */
    @PostMapping("/countUserByRoleType")
    public Result countUserByRoleType(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Integer status = Optional.ofNullable(params.get("status"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countUserByRoleType(status);
        return Result.success(data);
    }

    /**
     * 课程难度分布统计
     */
    @PostMapping("/countCourseByDifficulty")
    public Result countCourseByDifficulty(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Date startTime = null;
        String startTimeStr = (String) params.get("startTime");
        if (startTimeStr != null && !startTimeStr.isBlank()) {
            startTime = DateUtils.parseDate(startTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        Date endTime = null;
        String endTimeStr = (String) params.get("endTime");
        if (endTimeStr != null && !endTimeStr.isBlank()) {
            endTime = DateUtils.parseDate(endTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        Integer auditStatus = Optional.ofNullable(params.get("auditStatus"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countCourseByDifficulty(startTime, endTime, auditStatus);
        return Result.success(data);
    }

    /**
     * 题目类型分布统计
     */
    @PostMapping("/countQuestionByType")
    public Result countQuestionByType(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Integer difficultyLevel = Optional.ofNullable(params.get("difficultyLevel"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        Integer status = Optional.ofNullable(params.get("status"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countQuestionByType(difficultyLevel, status);
        return Result.success(data);
    }

    /**
     * 考试提交状态统计
     */
    @PostMapping("/countExamRecordBySubmitStatus")
    public Result countExamRecordBySubmitStatus(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Date startTime = null;
        String startTimeStr = (String) params.get("startTime");
        if (startTimeStr != null && !startTimeStr.isBlank()) {
            startTime = DateUtils.parseDate(startTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        Date endTime = null;
        String endTimeStr = (String) params.get("endTime");
        if (endTimeStr != null && !endTimeStr.isBlank()) {
            endTime = DateUtils.parseDate(endTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        List<EchartsDataVO> data = echartsService.countExamRecordBySubmitStatus(startTime, endTime);
        return Result.success(data);
    }

    /**
     * 不同难度试卷平均分统计
     */
    @PostMapping("/avgExamScoreByPaperDifficulty")
    public Result avgExamScoreByPaperDifficulty(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Date startTime = null;
        String startTimeStr = (String) params.get("startTime");
        if (startTimeStr != null && !startTimeStr.isBlank()) {
            startTime = DateUtils.parseDate(startTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        Date endTime = null;
        String endTimeStr = (String) params.get("endTime");
        if (endTimeStr != null && !endTimeStr.isBlank()) {
            endTime = DateUtils.parseDate(endTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        List<EchartsDataVO> data = echartsService.avgExamScoreByPaperDifficulty(startTime, endTime);
        return Result.success(data);
    }

    /**
     * 章节内容类型分布统计
     */
    @PostMapping("/countContentByContentType")
    public Result countContentByContentType(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Long courseId = Optional.ofNullable(params.get("courseId"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countContentByContentType(courseId);
        return Result.success(data);
    }

    /**
     * 试卷难度分布统计（全平台）
     */
    @PostMapping("/countPaperByDifficulty")
    public Result countPaperByDifficulty(@RequestBody(required = false) Map<String, Object> params) {
        List<EchartsDataVO> data = echartsService.countPaperByDifficulty();
        return Result.success(data);
    }

}
