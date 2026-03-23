package com.edu.teaching.module.edu_user.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_user.entity.EduUser;
import com.edu.teaching.module.edu_user.service.EduUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 系统用户Controller
 */
@RestController
@RequestMapping("/edu_user")
public class EduUserController {

    @Autowired
    private EduUserService eduUserService;

    /**
     * 分页查询系统用户列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduUser entity) {
        if (entity == null) {
            entity = new EduUser();
        }
        return Result.success(eduUserService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有系统用户列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduUser entity) {
        if (entity == null) {
            entity = new EduUser();
        }
        List<EduUser> list = eduUserService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询系统用户
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduUser eduUser = eduUserService.getById(id);
        if (eduUser == null) {
            return Result.error("系统用户不存在");
        }
        return Result.success(eduUser);
    }

    /**
     * 新增系统用户
     */
    @PostMapping
    public Result save(@RequestBody EduUser eduUser) {
        eduUser.setCreateTime(LocalDateTime.now());
        boolean success = eduUserService.save(eduUser);
        if (success) {
            return Result.success("新增成功", eduUser);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新系统用户
     */
    @PutMapping
    public Result update(@RequestBody EduUser eduUser) {
        boolean success = eduUserService.updateById(eduUser);
        if (success) {
            return Result.success("更新成功", eduUser);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除系统用户
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduUserService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除系统用户
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduUserService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
