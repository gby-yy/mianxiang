package com.edu.teaching.module.edu_user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_user.entity.EduUser;
import com.edu.teaching.module.edu_user.mapper.EduUserMapper;
import com.edu.teaching.module.edu_user.service.EduUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 系统用户Service实现类
 */
@Service
public class EduUserServiceImpl extends ServiceImpl<EduUserMapper, EduUser> implements EduUserService {

    @Autowired
    private EduUserMapper eduUserMapper;

    @Override
    public IPage<EduUser> pageQuery(Integer current, Integer size, EduUser entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduUser> records = eduUserMapper.pageQuery(entity, offset, limit);
        Long total = eduUserMapper.countQuery(entity);

        Page<EduUser> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduUser> listQuery(EduUser entity) {
        return eduUserMapper.listQuery(entity);
    }

    @Override
    public EduUser getById(Long id) {
        return eduUserMapper.getById(id);
    }

}
