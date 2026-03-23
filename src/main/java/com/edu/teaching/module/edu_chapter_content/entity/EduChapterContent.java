package com.edu.teaching.module.edu_chapter_content.entity;

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
 * @description: 章节学习内容实体类
 */
@Data
@TableName("edu_chapter_content")
public class EduChapterContent implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属章节 */
    private Long chapterId;
    /** 内容标题 */
    private String contentTitle;
    /** 内容类型 */
    private Integer contentType;
    /** 视频地址 */
    private String videoUrl;
    /** 文档地址 */
    private String docUrl;
    /** 封面图 */
    private String coverImage;
    /** 排序 */
    private Integer sortOrder;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 章节名称 */
    @TableField(exist = false)
    private String chapterName;


}
