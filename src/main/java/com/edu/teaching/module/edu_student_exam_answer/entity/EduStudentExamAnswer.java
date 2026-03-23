package com.edu.teaching.module.edu_student_exam_answer.entity;

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
 * @description: 学生考试记录详情实体类
 */
@Data
@TableName("edu_student_exam_answer")
public class EduStudentExamAnswer implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 考试记录 */
    private Long examRecordId;
    /** 题目 */
    private Long questionId;
    /** 学生答案 */
    private String studentAnswer;
    /** 正确答案 */
    private String correctAnswer;
    /** 是否正确 */
    private Integer isCorrect;
    /** 得分 */
    private BigDecimal score;
    /** AI评分状态 */
    private Integer aiScoreStatus;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 考试编号(gen:ER) */
    @TableField(exist = false)
    private String examNo;
    /** 题目标题 */
    @TableField(exist = false)
    private String questionTitle;


}
