package com.edu.teaching.basic.service;

import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;

import java.util.List;
import java.util.Map;

/**
 * 学生选课/开始学习：创建学习记录、复制章节、按基础类型解锁
 */
public interface StudentCourseLearnService {

    /**
     * 获取学生学习该课程的记录与章节列表（无则返回 null）
     */
    EduStudentCourseRecord getRecord(Long studentId, Long courseId);

    /**
     * 获取某条学习记录下的章节学习记录列表（按 chapter_order 升序）
     */
    List<EduStudentCourseChapterRecord> listChapterRecords(Long recordId);

    /**
     * 首次选课：创建学习记录、从课程复制章节到学生章节记录、按 foundationType 解锁
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @param foundationType 0=0基础（仅解锁第1章基础章节），1=有基础（解锁所有基础+第1章难度章节）
     * @return 新建的学习记录（含章节列表需再查）
     */
    EduStudentCourseRecord startLearn(Long studentId, Long courseId, Integer foundationType);

    /**
     * 上报某条内容已学习完成，更新 content_progress JSON 并重新计算课程学习进度
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @param contentId 内容ID（edu_chapter_content.id）
     * @return 更新后的学习记录，若未选课或记录不存在返回 null
     */
    EduStudentCourseRecord reportContentLearned(Long studentId, Long courseId, Long contentId);

    /**
     * 章节测试通过后解锁下一章
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @param afterChapterId 当前通过章节的 sourceChapterId（edu_course_chapter.id）
     * @return 被解锁的章节记录，若无下一章返回 null
     */
    EduStudentCourseChapterRecord unlockNextChapter(Long studentId, Long courseId, Long afterChapterId);

    /**
     * 学生首页看板：已选课程的学习进度与待办（已解锁章节数/总章节数、待学习章节、待测试章节）
     */
    List<Map<String, Object>> getDashboardOverview(Long studentId);
}
