package com.edu.teaching.module.edu_exam_paper_question.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_exam_paper_question.dto.AiGeneratePaperQuestionDTO;
import com.edu.teaching.module.edu_exam_paper_question.entity.EduExamPaperQuestion;
import com.edu.teaching.module.edu_exam_paper_question.service.EduExamPaperQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 试卷题目关联Controller
 */
@RestController
@RequestMapping("/edu_exam_paper_question")
public class EduExamPaperQuestionController {

    @Autowired
    private EduExamPaperQuestionService eduExamPaperQuestionService;

    /**
     * 分页查询试卷题目关联列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduExamPaperQuestion entity) {
        if (entity == null) {
            entity = new EduExamPaperQuestion();
        }
        return Result.success(eduExamPaperQuestionService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有试卷题目关联列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduExamPaperQuestion entity) {
        if (entity == null) {
            entity = new EduExamPaperQuestion();
        }
        List<EduExamPaperQuestion> list = eduExamPaperQuestionService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询试卷题目关联
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduExamPaperQuestion eduExamPaperQuestion = eduExamPaperQuestionService.getById(id);
        if (eduExamPaperQuestion == null) {
            return Result.error("试卷题目关联不存在");
        }
        return Result.success(eduExamPaperQuestion);
    }

    /**
     * 新增试卷题目关联
     */
    @PostMapping
    public Result save(@RequestBody EduExamPaperQuestion eduExamPaperQuestion) {
        eduExamPaperQuestion.setCreateTime(LocalDateTime.now());
        boolean success = eduExamPaperQuestionService.save(eduExamPaperQuestion);
        if (success) {
            return Result.success("新增成功", eduExamPaperQuestion);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新试卷题目关联
     */
    @PutMapping
    public Result update(@RequestBody EduExamPaperQuestion eduExamPaperQuestion) {
        boolean success = eduExamPaperQuestionService.updateById(eduExamPaperQuestion);
        if (success) {
            return Result.success("更新成功", eduExamPaperQuestion);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除试卷题目关联
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduExamPaperQuestionService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除试卷题目关联
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduExamPaperQuestionService.removeByIds(ids);
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
        List<SelectVO> list = eduExamPaperQuestionService.listEduChapterExamPaper();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：edu_question_bank
     */
    @GetMapping("/select/listEduQuestionBank")
    public Result listEduQuestionBank() {
        List<SelectVO> list = eduExamPaperQuestionService.listEduQuestionBank();
        return Result.success(list);
    }

    /**
     * AI生成试卷题目
     */
    @PostMapping("/ai-generate")
    public Result aiGeneratePaperQuestions(@RequestBody AiGeneratePaperQuestionDTO dto) {
        if (dto == null || dto.getPaperId() == null || dto.getDifficultyLevel() == null || dto.getQuestionCount() == null) {
            return Result.error("参数不完整");
        }
        if (dto.getAvailableQuestions() == null || dto.getAvailableQuestions().isEmpty()) {
            return Result.error("可用题目列表为空");
        }
        if (dto.getQuestionCount() <= 0 || dto.getQuestionCount() > 20) {
            return Result.error("题目数量必须在1-20之间");
        }

        try {
            List<Long> selectedQuestionIds = eduExamPaperQuestionService.aiGeneratePaperQuestions(
                    dto.getPaperId(),
                    dto.getDifficultyLevel(),
                    dto.getQuestionCount(),
                    dto.getAvailableQuestions()
            );

            if (selectedQuestionIds == null || selectedQuestionIds.isEmpty()) {
                return Result.error("AI生成失败，未选择任何题目");
            }

            return Result.success("生成成功", selectedQuestionIds);
        } catch (Exception e) {
            return Result.error("AI生成失败：" + e.getMessage());
        }
    }

}
