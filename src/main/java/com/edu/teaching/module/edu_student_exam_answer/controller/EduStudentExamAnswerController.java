package com.edu.teaching.module.edu_student_exam_answer.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import com.edu.teaching.module.edu_student_exam_answer.service.EduStudentExamAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 学生考试记录详情Controller
 */
@RestController
@RequestMapping("/edu_student_exam_answer")
public class EduStudentExamAnswerController {

    @Autowired
    private EduStudentExamAnswerService eduStudentExamAnswerService;

    /**
     * 分页查询学生考试记录详情列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduStudentExamAnswer entity) {
        if (entity == null) {
            entity = new EduStudentExamAnswer();
        }
        return Result.success(eduStudentExamAnswerService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有学生考试记录详情列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduStudentExamAnswer entity) {
        if (entity == null) {
            entity = new EduStudentExamAnswer();
        }
        List<EduStudentExamAnswer> list = eduStudentExamAnswerService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询学生考试记录详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduStudentExamAnswer eduStudentExamAnswer = eduStudentExamAnswerService.getById(id);
        if (eduStudentExamAnswer == null) {
            return Result.error("学生考试记录详情不存在");
        }
        return Result.success(eduStudentExamAnswer);
    }

    /**
     * 新增学生考试记录详情
     */
    @PostMapping
    public Result save(@RequestBody EduStudentExamAnswer eduStudentExamAnswer) {
        eduStudentExamAnswer.setCreateTime(LocalDateTime.now());
        boolean success = eduStudentExamAnswerService.save(eduStudentExamAnswer);
        if (success) {
            return Result.success("新增成功", eduStudentExamAnswer);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新学生考试记录详情
     */
    @PutMapping
    public Result update(@RequestBody EduStudentExamAnswer eduStudentExamAnswer) {
        boolean success = eduStudentExamAnswerService.updateById(eduStudentExamAnswer);
        if (success) {
            return Result.success("更新成功", eduStudentExamAnswer);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除学生考试记录详情
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduStudentExamAnswerService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除学生考试记录详情
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduStudentExamAnswerService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：edu_student_exam_record
     */
    @GetMapping("/select/listEduStudentExamRecord")
    public Result listEduStudentExamRecord() {
        List<SelectVO> list = eduStudentExamAnswerService.listEduStudentExamRecord();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：edu_question_bank
     */
    @GetMapping("/select/listEduQuestionBank")
    public Result listEduQuestionBank() {
        List<SelectVO> list = eduStudentExamAnswerService.listEduQuestionBank();
        return Result.success(list);
    }

}
