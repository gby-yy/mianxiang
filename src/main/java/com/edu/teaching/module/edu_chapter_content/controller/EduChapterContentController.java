package com.edu.teaching.module.edu_chapter_content.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_chapter_content.entity.EduChapterContent;
import com.edu.teaching.module.edu_chapter_content.service.EduChapterContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 章节学习内容Controller
 */
@RestController
@RequestMapping("/edu_chapter_content")
public class EduChapterContentController {

    @Autowired
    private EduChapterContentService eduChapterContentService;

    /**
     * 分页查询章节学习内容列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduChapterContent entity) {
        if (entity == null) {
            entity = new EduChapterContent();
        }
        return Result.success(eduChapterContentService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有章节学习内容列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduChapterContent entity) {
        if (entity == null) {
            entity = new EduChapterContent();
        }
        List<EduChapterContent> list = eduChapterContentService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询章节学习内容
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduChapterContent eduChapterContent = eduChapterContentService.getById(id);
        if (eduChapterContent == null) {
            return Result.error("章节学习内容不存在");
        }
        return Result.success(eduChapterContent);
    }

    /**
     * 新增章节学习内容
     */
    @PostMapping
    public Result save(@RequestBody EduChapterContent eduChapterContent) {
        eduChapterContent.setCreateTime(LocalDateTime.now());
        boolean success = eduChapterContentService.save(eduChapterContent);
        if (success) {
            return Result.success("新增成功", eduChapterContent);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新章节学习内容
     */
    @PutMapping
    public Result update(@RequestBody EduChapterContent eduChapterContent) {
        boolean success = eduChapterContentService.updateById(eduChapterContent);
        if (success) {
            return Result.success("更新成功", eduChapterContent);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除章节学习内容
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduChapterContentService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除章节学习内容
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduChapterContentService.removeByIds(ids);
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
        List<SelectVO> list = eduChapterContentService.listEduCourseChapter();
        return Result.success(list);
    }

}
