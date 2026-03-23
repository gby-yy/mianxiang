package com.edu.teaching.module.edu_student_user.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;
import com.edu.teaching.module.edu_student_user.service.EduStudentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 学生Controller
 */
@RestController
@RequestMapping("/edu_student_user")
public class EduStudentUserController {

    @Autowired
    private EduStudentUserService eduStudentUserService;

    /**
     * 分页查询学生列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduStudentUser entity) {
        if (entity == null) {
            entity = new EduStudentUser();
        }
        return Result.success(eduStudentUserService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有学生列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduStudentUser entity) {
        if (entity == null) {
            entity = new EduStudentUser();
        }
        List<EduStudentUser> list = eduStudentUserService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询学生
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduStudentUser eduStudentUser = eduStudentUserService.getById(id);
        if (eduStudentUser == null) {
            return Result.error("学生不存在");
        }
        return Result.success(eduStudentUser);
    }

    /**
     * 新增学生
     */
    @PostMapping
    public Result save(@RequestBody EduStudentUser eduStudentUser) {
        eduStudentUser.setCreateTime(LocalDateTime.now());
        boolean success = eduStudentUserService.save(eduStudentUser);
        if (success) {
            return Result.success("新增成功", eduStudentUser);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新学生
     */
    @PutMapping
    public Result update(@RequestBody EduStudentUser eduStudentUser) {
        boolean success = eduStudentUserService.updateById(eduStudentUser);
        if (success) {
            return Result.success("更新成功", eduStudentUser);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除学生
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduStudentUserService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除学生
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduStudentUserService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
