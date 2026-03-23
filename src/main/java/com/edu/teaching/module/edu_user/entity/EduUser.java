package com.edu.teaching.module.edu_user.entity;

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
 * @description: 系统用户实体类
 */
@Data
@TableName("edu_user")
public class EduUser implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 登录账号 */
    private String username;
    /** 登录密码 */
    private String password;
    /** 姓名 */
    private String realName;
    /** 用户角色 */
    private String roleType;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 头像 */
    private String avatar;
    /** 性别 */
    private Integer gender;
    /** 教师职称 */
    private Integer titleLevel;
    /** 教龄 */
    private Integer teachYears;
    /** 个人简介 */
    private String intro;
    /** 账号状态 */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
