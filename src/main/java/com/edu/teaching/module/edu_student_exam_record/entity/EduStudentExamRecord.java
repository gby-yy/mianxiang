package com.edu.teaching.module.edu_student_exam_record.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * @author wuming
 * @description: 学生考试记录实体类
 */
@Data
@TableName("edu_student_exam_record")
public class EduStudentExamRecord implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 考试编号(gen:ER) */
    private String examNo;
    /** 考试试卷 */
    private Long paperId;
    /** 学生 */
    private Long studentId;
    /** 提交状态 */
    private Integer submitStatus;
    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    /** 客观题得分 */
    private BigDecimal objectiveScore;
    /** 主观题得分 */
    private BigDecimal subjectiveScore;
    /** 总得分 */
    private BigDecimal totalScore;
    /** 正确率 */
    private BigDecimal correctRate;
    /** 答题时长秒 */
    private Integer durationSeconds;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 试卷名称 */
    @TableField(exist = false)
    private String paperName;
    /** 学生姓名 */
    @TableField(exist = false)
    private String realName;
    /** 课程名称（查询关联） */
    @TableField(exist = false)
    private String courseName;
    /** 章节ID（查询关联，用于课程分析按章节聚合） */
    @TableField(exist = false)
    private Long chapterId;
    /** 章节名称（查询关联） */
    @TableField(exist = false)
    private String chapterName;
    /** 试卷总分（查询关联） */
    @TableField(exist = false)
    private BigDecimal paperTotalScore;

}
