package com.edu.teaching.module.edu_student_course_chapter_record.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生课程章节学习记录
 */
@Data
@TableName("edu_student_course_chapter_record")
public class EduStudentCourseChapterRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 学习记录ID */
    private Long recordId;
    /** 学生ID */
    private Long studentId;
    /** 课程ID */
    private Long courseId;
    /** 来源章节ID */
    private Long sourceChapterId;
    /** 章节名称 */
    private String chapterName;
    /** 章节类型 1:基础章节 2:难度章节 */
    private Integer chapterType;
    /** 章节难度 1:简单 2:中等 3:困难 */
    private Integer difficultyLevel;
    /** 章节排序 */
    private Integer chapterOrder;
    /** 解锁状态 0:未解锁 1:已解锁 */
    private Integer unlockStatus;
    /** 学习状态 0:未学习 1:学习中 2:已完成 */
    private Integer studyStatus;
    /** 内容数量 */
    private Integer contentCount;
    /** 题目数量 */
    private Integer questionCount;
    /** 章节进度 */
    private BigDecimal studyProgressRate;
    /** 开始学习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /** 解锁时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unlockTime;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
    /** 最近学习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLearnTime;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
