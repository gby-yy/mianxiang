package com.edu.teaching.module.edu_chapter_exam_paper.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_chapter_exam_paper.entity.EduChapterExamPaper;
import com.edu.teaching.module.edu_chapter_exam_paper.service.EduChapterExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 章节试卷Controller
 */
@RestController
@RequestMapping("/edu_chapter_exam_paper")
public class EduChapterExamPaperController {

    @Autowired
    private EduChapterExamPaperService eduChapterExamPaperService;

    /**
     * 分页查询章节试卷列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduChapterExamPaper entity) {
        if (entity == null) {
            entity = new EduChapterExamPaper();
        }
        return Result.success(eduChapterExamPaperService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有章节试卷列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduChapterExamPaper entity) {
        if (entity == null) {
            entity = new EduChapterExamPaper();
        }
        List<EduChapterExamPaper> list = eduChapterExamPaperService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询章节试卷
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduChapterExamPaper eduChapterExamPaper = eduChapterExamPaperService.getById(id);
        if (eduChapterExamPaper == null) {
            return Result.error("章节试卷不存在");
        }
        return Result.success(eduChapterExamPaper);
    }

    /**
     * 新增章节试卷
     */
    @PostMapping
    public Result save(@RequestBody EduChapterExamPaper eduChapterExamPaper) {
        eduChapterExamPaper.setCreateTime(LocalDateTime.now());
        // 如果试卷编号为空，自动生成
        if (eduChapterExamPaper.getPaperCode() == null || eduChapterExamPaper.getPaperCode().trim().isEmpty()) {
            // 生成格式：EP + 时间戳后8位
            String timestamp = String.valueOf(System.currentTimeMillis());
            String codeSuffix = timestamp.substring(timestamp.length() - 8);
            eduChapterExamPaper.setPaperCode("EP" + codeSuffix);
        }
        boolean success = eduChapterExamPaperService.save(eduChapterExamPaper);
        if (success) {
            return Result.success("新增成功", eduChapterExamPaper);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新章节试卷
     */
    @PutMapping
    public Result update(@RequestBody EduChapterExamPaper eduChapterExamPaper) {
        boolean success = eduChapterExamPaperService.updateById(eduChapterExamPaper);
        if (success) {
            return Result.success("更新成功", eduChapterExamPaper);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除章节试卷
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduChapterExamPaperService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除章节试卷
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduChapterExamPaperService.removeByIds(ids);
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
        List<SelectVO> list = eduChapterExamPaperService.listEduCourseChapter();
        return Result.success(list);
    }

}
