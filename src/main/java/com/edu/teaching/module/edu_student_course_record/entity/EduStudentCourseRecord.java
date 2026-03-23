package com.edu.teaching.module.edu_student_course_record.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生课程学习记录
 */
@Data
@TableName("edu_student_course_record")
public class EduStudentCourseRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 学生ID */
    private Long studentId;
    /** 课程ID */
    private Long courseId;
    /** 当前学习章节ID */
    private Long currentChapterId;
    /** 总章节数 */
    private Integer chapterTotalCount;
    /** 已完成章节数 */
    private Integer finishedChapterCount;
    /** 学习进度 */
    private BigDecimal progressRate;
    /** 内容学习进度 JSON，如 {"1":1,"2":1} 表示内容ID 1、2 已学习，值为 1 表示已学习，用于回显与计算进度 */
    private String contentProgress;
    /** 学习状态 1:学习中 2:已完成 */
    private Integer learnStatus;
    /** 最近学习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLearnTime;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
