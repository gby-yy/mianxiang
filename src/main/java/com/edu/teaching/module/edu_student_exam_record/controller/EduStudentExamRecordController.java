package com.edu.teaching.module.edu_student_exam_record.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import com.edu.teaching.module.edu_student_exam_answer.service.EduStudentExamAnswerService;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_exam_record.service.EduStudentExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 学生考试记录Controller
 */
@RestController
@RequestMapping("/edu_student_exam_record")
public class EduStudentExamRecordController {

    @Autowired
    private EduStudentExamRecordService eduStudentExamRecordService;
    @Autowired
    private EduStudentExamAnswerService eduStudentExamAnswerService;

    /**
     * 分页查询学生考试记录列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduStudentExamRecord entity) {
        if (entity == null) {
            entity = new EduStudentExamRecord();
        }
        return Result.success(eduStudentExamRecordService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有学生考试记录列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduStudentExamRecord entity) {
        if (entity == null) {
            entity = new EduStudentExamRecord();
        }
        List<EduStudentExamRecord> list = eduStudentExamRecordService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询学生考试记录
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduStudentExamRecord eduStudentExamRecord = eduStudentExamRecordService.getById(id);
        if (eduStudentExamRecord == null) {
            return Result.error("学生考试记录不存在");
        }
        return Result.success(eduStudentExamRecord);
    }

    /**
     * 查询某条考试记录的学生提交记录（答题详情，教师/管理员用）
     */
    @GetMapping("/{id}/detail")
    public Result getRecordDetail(@PathVariable Long id) {
        EduStudentExamRecord record = eduStudentExamRecordService.getById(id);
        if (record == null) {
            return Result.error("考试记录不存在");
        }
        EduStudentExamAnswer query = new EduStudentExamAnswer();
        query.setExamRecordId(id);
        List<EduStudentExamAnswer> answers = eduStudentExamAnswerService.listQuery(query);
        Map<String, Object> data = new HashMap<>();
        data.put("record", record);
        data.put("answers", answers);
        return Result.success(data);
    }

    /**
     * 新增学生考试记录
     */
    @PostMapping
    public Result save(@RequestBody EduStudentExamRecord eduStudentExamRecord) {
        eduStudentExamRecord.setCreateTime(LocalDateTime.now());
        boolean success = eduStudentExamRecordService.save(eduStudentExamRecord);
        if (success) {
            return Result.success("新增成功", eduStudentExamRecord);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新学生考试记录
     */
    @PutMapping
    public Result update(@RequestBody EduStudentExamRecord eduStudentExamRecord) {
        boolean success = eduStudentExamRecordService.updateById(eduStudentExamRecord);
        if (success) {
            return Result.success("更新成功", eduStudentExamRecord);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除学生考试记录
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduStudentExamRecordService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除学生考试记录
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduStudentExamRecordService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：edu_chapter_exam_paper
     */
    @GetMapping("/select/listEduChapterExamPaper")
    public Result listEduChapterExamPaper() {
        List<SelectVO> list = eduStudentExamRecordService.listEduChapterExamPaper();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：edu_student_user
     */
    @GetMapping("/select/listEduStudentUser")
    public Result listEduStudentUser() {
        List<SelectVO> list = eduStudentExamRecordService.listEduStudentUser();
        return Result.success(list);
    }

}
