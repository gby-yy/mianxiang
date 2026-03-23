package com.edu.teaching.module.edu_course.entity;

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
 * @description: 课程实体类
 */
@Data
@TableName("edu_course")
public class EduCourse implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 课程编号(gen:CR) */
    private String courseCode;
    /** 课程名称 */
    private String courseName;
    /** 课程教师 */
    private Long teacherId;
    /** 课程封面 */
    private String cover;
    /** 课程简介 */
    private String courseDesc;
    /** 课程难度 */
    private Integer difficultyLevel;
    /** 章节数量 */
    private Integer chapterCount;
    /** 审核状态 */
    private Integer auditStatus;
    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /** 驳回原因 */
    private String rejectReason;
    /** 课程状态 */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 姓名 */
    @TableField(exist = false)
    private String realName;


}
