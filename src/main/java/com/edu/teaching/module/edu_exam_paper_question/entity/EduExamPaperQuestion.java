package com.edu.teaching.module.edu_exam_paper_question.entity;

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
 * @description: 试卷题目关联实体类
 */
@Data
@TableName("edu_exam_paper_question")
public class EduExamPaperQuestion implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属试卷 */
    private Long paperId;
    /** 关联题目 */
    private Long questionId;
    /** 题目分值 */
    private BigDecimal questionScore;
    /** 排序 */
    private Integer sortOrder;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 试卷名称 */
    @TableField(exist = false)
    private String paperName;
    /** 题目标题 */
    @TableField(exist = false)
    private String questionTitle;
    /** 题目类型 */
    @TableField(exist = false)
    private Integer questionType;
    /** 难度等级 */
    @TableField(exist = false)
    private Integer difficultyLevel;


}
