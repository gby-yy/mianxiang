package com.edu.teaching.module.edu_chapter_exam_paper.entity;

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
 * @description: 章节试卷实体类
 */
@Data
@TableName("edu_chapter_exam_paper")
public class EduChapterExamPaper implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 试卷编号(gen:EP) */
    private String paperCode;
    /** 所属章节 */
    private Long chapterId;
    /** 试卷名称 */
    private String paperName;
    /** 试卷难度 */
    private Integer difficultyLevel;
    /** 题目数量 */
    private Integer questionCount;
    /** 总分 */
    private BigDecimal totalScore;
    /** 考试时长 */
    private Integer durationMinutes;
    /** 生成方式 */
    private Integer generateType;
    /** 试卷说明 */
    private String paperDesc;
    /** 试卷状态 */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 章节名称 */
    @TableField(exist = false)
    private String chapterName;
    /** 教师ID（查询用：仅展示该教师所属课程下的试卷） */
    @TableField(exist = false)
    private Long teacherId;

}
