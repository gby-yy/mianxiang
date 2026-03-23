package com.edu.teaching.module.edu_student_user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;

import java.util.List;

/**
 * @author wuming
 * @description: 学生Service接口
 */
public interface EduStudentUserService extends IService<EduStudentUser> {

    /**
     * 分页查询
     */
    IPage<EduStudentUser> pageQuery(Integer current, Integer size, EduStudentUser entity);

    /**
     * 列表查询（不分页）
     */
    List<EduStudentUser> listQuery(EduStudentUser entity);

    /**
     * 根据ID查询
     */
    EduStudentUser getById(Long id);

}
