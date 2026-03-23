package com.edu.teaching.module.edu_question_option.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_question_option.entity.EduQuestionOption;
import com.edu.teaching.module.edu_question_option.service.EduQuestionOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 题目选项Controller
 */
@RestController
@RequestMapping("/edu_question_option")
public class EduQuestionOptionController {

    @Autowired
    private EduQuestionOptionService eduQuestionOptionService;

    /**
     * 分页查询题目选项列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduQuestionOption entity) {
        if (entity == null) {
            entity = new EduQuestionOption();
        }
        return Result.success(eduQuestionOptionService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有题目选项列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduQuestionOption entity) {
        if (entity == null) {
            entity = new EduQuestionOption();
        }
        List<EduQuestionOption> list = eduQuestionOptionService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询题目选项
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduQuestionOption eduQuestionOption = eduQuestionOptionService.getById(id);
        if (eduQuestionOption == null) {
            return Result.error("题目选项不存在");
        }
        return Result.success(eduQuestionOption);
    }

    /**
     * 新增题目选项
     */
    @PostMapping
    public Result save(@RequestBody EduQuestionOption eduQuestionOption) {
        eduQuestionOption.setCreateTime(LocalDateTime.now());
        boolean success = eduQuestionOptionService.save(eduQuestionOption);
        if (success) {
            return Result.success("新增成功", eduQuestionOption);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新题目选项
     */
    @PutMapping
    public Result update(@RequestBody EduQuestionOption eduQuestionOption) {
        boolean success = eduQuestionOptionService.updateById(eduQuestionOption);
        if (success) {
            return Result.success("更新成功", eduQuestionOption);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除题目选项
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduQuestionOptionService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除题目选项
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduQuestionOptionService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：edu_question_bank
     */
    @GetMapping("/select/listEduQuestionBank")
    public Result listEduQuestionBank() {
        List<SelectVO> list = eduQuestionOptionService.listEduQuestionBank();
        return Result.success(list);
    }

}
