package com.edu.teaching.module.edu_question_bank.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.module.edu_question_bank.service.EduQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 章节题库Controller
 */
@RestController
@RequestMapping("/edu_question_bank")
public class EduQuestionBankController {

    @Autowired
    private EduQuestionBankService eduQuestionBankService;

    /**
     * 分页查询章节题库列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduQuestionBank entity) {
        if (entity == null) {
            entity = new EduQuestionBank();
        }
        return Result.success(eduQuestionBankService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有章节题库列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduQuestionBank entity) {
        if (entity == null) {
            entity = new EduQuestionBank();
        }
        List<EduQuestionBank> list = eduQuestionBankService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询章节题库
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduQuestionBank eduQuestionBank = eduQuestionBankService.getById(id);
        if (eduQuestionBank == null) {
            return Result.error("章节题库不存在");
        }
        return Result.success(eduQuestionBank);
    }

    /**
     * 新增章节题库
     */
    @PostMapping
    public Result save(@RequestBody EduQuestionBank eduQuestionBank) {
        eduQuestionBank.setCreateTime(LocalDateTime.now());
        boolean success = eduQuestionBankService.save(eduQuestionBank);
        if (success) {
            return Result.success("新增成功", eduQuestionBank);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新章节题库
     */
    @PutMapping
    public Result update(@RequestBody EduQuestionBank eduQuestionBank) {
        boolean success = eduQuestionBankService.updateById(eduQuestionBank);
        if (success) {
            return Result.success("更新成功", eduQuestionBank);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除章节题库
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduQuestionBankService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除章节题库
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduQuestionBankService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：edu_course_chapter
     */
    @GetMapping("/select/listEduCourseChapter")
    public Result listEduCourseChapter() {
        List<SelectVO> list = eduQuestionBankService.listEduCourseChapter();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：edu_course
     */
    @GetMapping("/select/listEduCourse")
    public Result listEduCourse() {
        List<SelectVO> list = eduQuestionBankService.listEduCourse();
        return Result.success(list);
    }

}
