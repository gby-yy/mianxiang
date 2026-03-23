package com.edu.teaching.module.edu_user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_user.entity.EduUser;

import java.util.List;

/**
 * @author wuming
 * @description: 系统用户Service接口
 */
public interface EduUserService extends IService<EduUser> {

    /**
     * 分页查询
     */
    IPage<EduUser> pageQuery(Integer current, Integer size, EduUser entity);

    /**
     * 列表查询（不分页）
     */
    List<EduUser> listQuery(EduUser entity);

    /**
     * 根据ID查询
     */
    EduUser getById(Long id);

}
