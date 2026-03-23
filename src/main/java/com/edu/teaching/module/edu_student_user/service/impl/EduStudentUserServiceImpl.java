package com.edu.teaching.module.edu_student_user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;
import com.edu.teaching.module.edu_student_user.mapper.EduStudentUserMapper;
import com.edu.teaching.module.edu_student_user.service.EduStudentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 学生Service实现类
 */
@Service
public class EduStudentUserServiceImpl extends ServiceImpl<EduStudentUserMapper, EduStudentUser> implements EduStudentUserService {

    @Autowired
    private EduStudentUserMapper eduStudentUserMapper;

    @Override
    public IPage<EduStudentUser> pageQuery(Integer current, Integer size, EduStudentUser entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduStudentUser> records = eduStudentUserMapper.pageQuery(entity, offset, limit);
        Long total = eduStudentUserMapper.countQuery(entity);

        Page<EduStudentUser> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduStudentUser> listQuery(EduStudentUser entity) {
        return eduStudentUserMapper.listQuery(entity);
    }

    @Override
    public EduStudentUser getById(Long id) {
        return eduStudentUserMapper.getById(id);
    }

}
