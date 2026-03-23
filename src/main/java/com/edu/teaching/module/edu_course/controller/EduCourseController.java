package com.edu.teaching.module.edu_course.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.module.edu_course.service.EduCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 课程Controller
 */
@RestController
@RequestMapping("/edu_course")
public class EduCourseController {

    @Autowired
    private EduCourseService eduCourseService;

    /**
     * 分页查询课程列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduCourse entity) {
        if (entity == null) {
            entity = new EduCourse();
        }
        return Result.success(eduCourseService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有课程列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduCourse entity) {
        if (entity == null) {
            entity = new EduCourse();
        }
        List<EduCourse> list = eduCourseService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询课程
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduCourse eduCourse = eduCourseService.getById(id);
        if (eduCourse == null) {
            return Result.error("课程不存在");
        }
        return Result.success(eduCourse);
    }

    /**
     * 新增课程
     */
    @PostMapping
    public Result save(@RequestBody EduCourse eduCourse) {
        eduCourse.setCreateTime(LocalDateTime.now());
        boolean success = eduCourseService.save(eduCourse);
        if (success) {
            return Result.success("新增成功", eduCourse);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新课程
     */
    @PutMapping
    public Result update(@RequestBody EduCourse eduCourse) {
        boolean success = eduCourseService.updateById(eduCourse);
        if (success) {
            return Result.success("更新成功", eduCourse);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除课程
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduCourseService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除课程
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduCourseService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：edu_user
     */
    @GetMapping("/select/listEduUser")
    public Result listEduUser() {
        List<SelectVO> list = eduCourseService.listEduUser();
        return Result.success(list);
    }

}
