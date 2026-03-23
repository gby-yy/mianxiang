package com.edu.teaching.module.edu_course_chapter.entity;

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
 * @description: 课程章节实体类
 */
@Data
@TableName("edu_course_chapter")
public class EduCourseChapter implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属课程 */
    private Long courseId;
    /** 章节名称 */
    private String chapterName;
    /** 章节类型 */
    private Integer chapterType;
    /** 章节难度 */
    private Integer difficultyLevel;
    /** 章节排序 */
    private Integer chapterOrder;
    /** 章节说明 */
    private String chapterDesc;
    /** 内容数量 */
    private Integer contentCount;
    /** 测试题数量 */
    private Integer questionCount;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 课程名称 */
    @TableField(exist = false)
    private String courseName;


}
