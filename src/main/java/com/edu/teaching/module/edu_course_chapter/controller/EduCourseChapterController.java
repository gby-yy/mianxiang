package com.edu.teaching.module.edu_course_chapter.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_course_chapter.entity.EduCourseChapter;
import com.edu.teaching.module.edu_course_chapter.service.EduCourseChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

/**
 * @author wuming
 * @description: 课程章节Controller
 */
@RestController
@RequestMapping("/edu_course_chapter")
public class EduCourseChapterController {

    @Autowired
    private EduCourseChapterService eduCourseChapterService;

    /**
     * 分页查询课程章节列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) EduCourseChapter entity) {
        if (entity == null) {
            entity = new EduCourseChapter();
        }
        return Result.success(eduCourseChapterService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有课程章节列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) EduCourseChapter entity) {
        if (entity == null) {
            entity = new EduCourseChapter();
        }
        List<EduCourseChapter> list = eduCourseChapterService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询课程章节
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EduCourseChapter eduCourseChapter = eduCourseChapterService.getById(id);
        if (eduCourseChapter == null) {
            return Result.error("课程章节不存在");
        }
        return Result.success(eduCourseChapter);
    }

    /**
     * 新增课程章节
     */
    @PostMapping
    public Result save(@RequestBody EduCourseChapter eduCourseChapter) {
        eduCourseChapter.setCreateTime(LocalDateTime.now());
        boolean success = eduCourseChapterService.save(eduCourseChapter);
        if (success) {
            return Result.success("新增成功", eduCourseChapter);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新课程章节
     */
    @PutMapping
    public Result update(@RequestBody EduCourseChapter eduCourseChapter) {
        boolean success = eduCourseChapterService.updateById(eduCourseChapter);
        if (success) {
            return Result.success("更新成功", eduCourseChapter);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除课程章节
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = eduCourseChapterService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除课程章节
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = eduCourseChapterService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：edu_course
     */
    @GetMapping("/select/listEduCourse")
    public Result listEduCourse() {
        List<SelectVO> list = eduCourseChapterService.listEduCourse();
        return Result.success(list);
    }

}
