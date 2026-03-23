package com.edu.teaching.module.edu_question_option.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wuming
 * @description: 题目选项实体类
 */
@Data
@TableName("edu_question_option")
public class EduQuestionOption implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属题目 */
    private Long questionId;
    /** 选项标识 */
    private String optionLabel;
    /** 选项内容 */
    private String optionContent;
    /** 是否正确 */
    private Integer isCorrect;
    /** 排序 */
    private Integer sortOrder;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 题目标题 */
    @TableField(exist = false)
    private String questionTitle;


}
