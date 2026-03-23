package com.edu.teaching.module.edu_course.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.module.edu_course.mapper.EduCourseMapper;
import com.edu.teaching.module.edu_course.service.EduCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 课程Service实现类
 */
@Service
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements EduCourseService {

    @Autowired
    private EduCourseMapper eduCourseMapper;

    @Override
    public IPage<EduCourse> pageQuery(Integer current, Integer size, EduCourse entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduCourse> records = eduCourseMapper.pageQuery(entity, offset, limit);
        Long total = eduCourseMapper.countQuery(entity);

        Page<EduCourse> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduCourse> listQuery(EduCourse entity) {
        return eduCourseMapper.listQuery(entity);
    }

    @Override
    public EduCourse getById(Long id) {
        return eduCourseMapper.getById(id);
    }

    /**
     * 下拉数据：edu_user
     */
    @Override
    public List<SelectVO> listEduUser() {
        return eduCourseMapper.listEduUser();
    }

}
