package com.edu.teaching.module.edu_course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 课程Service接口
 */
public interface EduCourseService extends IService<EduCourse> {

    /**
     * 分页查询
     */
    IPage<EduCourse> pageQuery(Integer current, Integer size, EduCourse entity);

    /**
     * 列表查询（不分页）
     */
    List<EduCourse> listQuery(EduCourse entity);

    /**
     * 根据ID查询
     */
    EduCourse getById(Long id);

    /**
     * 下拉数据：edu_user
     */
    List<SelectVO> listEduUser();

}
