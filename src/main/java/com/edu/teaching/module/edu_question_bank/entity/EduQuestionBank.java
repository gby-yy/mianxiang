package com.edu.teaching.module.edu_question_bank.entity;

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
 * @description: 章节题库实体类
 */
@Data
@TableName("edu_question_bank")
public class EduQuestionBank implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属课程 */
    private Long courseId;
    /** 所属章节 */
    private Long chapterId;
    /** 题目标题 */
    private String questionTitle;
    /** 题目类型 */
    private Integer questionType;
    /** 难度等级 */
    private Integer difficultyLevel;
    /** 答案内容 */
    private String answerContent;
    /** 解析 */
    private String analysisContent;
    /** 题目分值 */
    private BigDecimal score;
    /** 题目状态 */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 课程名称 */
    @TableField(exist = false)
    private String courseName;
    /** 章节名称 */
    @TableField(exist = false)
    private String chapterName;
    /** 教师ID（查询用：仅展示该教师所属课程下的题目） */
    @TableField(exist = false)
    private Long teacherId;

}
