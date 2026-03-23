package com.edu.teaching.module.edu_question_bank.controller;

import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.module.edu_question_bank.service.EduQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.edu.teaching.vo.SelectVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.edu.teaching.util.ExcelQuestionParser;
import com.edu.teaching.module.edu_course.service.EduCourseService;
import com.edu.teaching.module.edu_course.entity.EduCourse;

/**
 * @author wuming
 * @description: 章节题库Controller
 */
@Slf4j
@RestController
@RequestMapping("/edu_question_bank")
public class EduQuestionBankController {

    @Autowired
    private EduQuestionBankService eduQuestionBankService;

    @Autowired
    private EduCourseService eduCourseService;

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


    /**
     * Excel 批量导入题目
     */
    @PostMapping("/import")
    public Result importQuestions(@RequestParam("file") MultipartFile file,
                                  @RequestParam Long courseId,
                                  @RequestParam Long chapterId,
                                  HttpServletRequest request) {
        log.info("开始批量导入题目，课程ID: {}, 章节ID: {}, 文件名: {}",
                courseId, chapterId, file.getOriginalFilename());

        // 1. 校验文件
        if (file.isEmpty()) {
            log.warn("上传文件为空");
            return Result.error("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            log.warn("文件格式错误: {}", fileName);
            return Result.error("请上传 Excel 文件 (.xlsx 或 .xls)");
        }

        // 2. 校验课程是否存在（可选，如果需要权限校验）
        try {
            EduCourse course = eduCourseService.getById(courseId);
            if (course == null) {
                log.warn("课程不存在，ID: {}", courseId);
                return Result.error("课程不存在");
            }

            // TODO: 这里可以添加权限校验，确保用户只能导入自己课程的题目
            // EduUserLoginController.LoginUser user = getUser(request);
            // if (!user.getId().equals(course.getTeacherId())) {
            //     return Result.error("无权为该课程导入题目");
            // }

        } catch (Exception e) {
            log.error("校验课程失败", e);
            return Result.error("校验课程失败");
        }

        try {
            // 3. 解析 Excel
            byte[] bytes = file.getBytes();
            List<EduQuestionBank> questions = ExcelQuestionParser.parse(bytes);

            if (questions.isEmpty()) {
                log.warn("Excel 中没有有效的题目数据");
                return Result.error("Excel 中没有有效的题目数据");
            }

            // 4. 设置课程和章节ID
            for (EduQuestionBank question : questions) {
                question.setCourseId(courseId);
                question.setChapterId(chapterId);
            }

            // 5. 批量保存
            boolean success = eduQuestionBankService.saveBatch(questions);

            if (success) {
                log.info("批量导入成功，共导入 {} 道题目", questions.size());
                return Result.success("成功导入 " + questions.size() + " 道题目", questions);
            } else {
                log.error("批量导入失败");
                return Result.error("导入失败，请检查数据格式");
            }

        } catch (IOException e) {
            log.error("文件解析失败", e);
            return Result.error("文件解析失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("导入题目异常", e);
            return Result.error("系统异常: " + e.getMessage());
        }
    }
}
