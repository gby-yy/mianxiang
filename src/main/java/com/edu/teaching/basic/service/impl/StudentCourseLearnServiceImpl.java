package com.edu.teaching.basic.service.impl;

import com.edu.teaching.basic.service.StudentCourseLearnService;
import com.edu.teaching.module.edu_chapter_content.entity.EduChapterContent;
import com.edu.teaching.module.edu_chapter_content.service.EduChapterContentService;
import com.edu.teaching.module.edu_course.entity.EduCourse;
import com.edu.teaching.module.edu_course.service.EduCourseService;
import com.edu.teaching.module.edu_course_chapter.entity.EduCourseChapter;
import com.edu.teaching.module.edu_course_chapter.service.EduCourseChapterService;
import com.edu.teaching.module.edu_student_course_chapter_record.entity.EduStudentCourseChapterRecord;
import com.edu.teaching.module.edu_student_course_chapter_record.service.EduStudentCourseChapterRecordService;
import com.edu.teaching.module.edu_student_course_record.entity.EduStudentCourseRecord;
import com.edu.teaching.module.edu_student_course_record.service.EduStudentCourseRecordService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生选课/开始学习：创建学习记录、复制章节、按基础类型解锁
 * 章节类型：1=基础章节，2=难度章节
 */
@Service
public class StudentCourseLearnServiceImpl implements StudentCourseLearnService {

    private static final int CHAPTER_TYPE_BASIC = 1;
    private static final int CHAPTER_TYPE_DIFFICULTY = 2;
    /** 0基础：仅解锁第1章（基础章节） */
    private static final int FOUNDATION_ZERO = 0;
    /** 有基础：解锁所有基础 + 第1章难度 */
    private static final int FOUNDATION_HAVE = 1;

    @Autowired
    private EduStudentCourseRecordService eduStudentCourseRecordService;
    @Autowired
    private EduStudentCourseChapterRecordService eduStudentCourseChapterRecordService;
    @Autowired
    private EduCourseChapterService eduCourseChapterService;
    @Autowired
    private EduChapterContentService eduChapterContentService;
    @Autowired
    private EduCourseService eduCourseService;

    @Override
    public EduStudentCourseRecord getRecord(Long studentId, Long courseId) {
        return eduStudentCourseRecordService.getByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public List<EduStudentCourseChapterRecord> listChapterRecords(Long recordId) {
        EduStudentCourseChapterRecord query = new EduStudentCourseChapterRecord();
        query.setRecordId(recordId);
        List<EduStudentCourseChapterRecord> list = eduStudentCourseChapterRecordService.listQuery(query);
        return list.stream()
            .sorted(Comparator.comparing(EduStudentCourseChapterRecord::getChapterOrder, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduStudentCourseRecord startLearn(Long studentId, Long courseId, Integer foundationType) {
        EduStudentCourseRecord existing = eduStudentCourseRecordService.getByStudentIdAndCourseId(studentId, courseId);
        if (existing != null) {
            throw new com.edu.teaching.common.BusinessException("您已选过该课程，请直接学习");
        }
        EduCourseChapter query = new EduCourseChapter();
        query.setCourseId(courseId);
        List<EduCourseChapter> chapters = eduCourseChapterService.listQuery(query);
        chapters = chapters.stream()
            .sorted(Comparator.comparing(EduCourseChapter::getChapterOrder, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
        if (chapters.isEmpty()) {
            throw new com.edu.teaching.common.BusinessException("该课程暂无章节，暂无法开始学习");
        }

        LocalDateTime now = LocalDateTime.now();
        EduStudentCourseRecord record = new EduStudentCourseRecord();
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        record.setChapterTotalCount(chapters.size());
        record.setFinishedChapterCount(0);
        record.setProgressRate(BigDecimal.ZERO);
        record.setLearnStatus(1);
        record.setLastLearnTime(now);
        record.setCreateTime(now);
        if (chapters.get(0) != null) {
            record.setCurrentChapterId(chapters.get(0).getId());
        }
        eduStudentCourseRecordService.save(record);
        Long recordId = record.getId();

        List<EduCourseChapter> basicChapters = chapters.stream().filter(c -> CHAPTER_TYPE_BASIC == c.getChapterType()).collect(Collectors.toList());
        List<EduCourseChapter> difficultyChapters = chapters.stream().filter(c -> CHAPTER_TYPE_DIFFICULTY == c.getChapterType()).collect(Collectors.toList());
        EduCourseChapter firstBasic = basicChapters.isEmpty() ? null : basicChapters.get(0);
        EduCourseChapter firstDifficulty = difficultyChapters.isEmpty() ? null : difficultyChapters.get(0);

        List<Long> unlockSourceIds = new ArrayList<>();
        if (foundationType == null || foundationType == FOUNDATION_ZERO) {
            if (firstBasic != null) {
                unlockSourceIds.add(firstBasic.getId());
            }
            if (unlockSourceIds.isEmpty() && !chapters.isEmpty()) {
                unlockSourceIds.add(chapters.get(0).getId());
            }
        } else {
            for (EduCourseChapter c : basicChapters) {
                unlockSourceIds.add(c.getId());
            }
            if (firstDifficulty != null) {
                unlockSourceIds.add(firstDifficulty.getId());
            }
        }

        for (EduCourseChapter ch : chapters) {
            EduStudentCourseChapterRecord cr = new EduStudentCourseChapterRecord();
            cr.setRecordId(recordId);
            cr.setStudentId(studentId);
            cr.setCourseId(courseId);
            cr.setSourceChapterId(ch.getId());
            cr.setChapterName(ch.getChapterName());
            cr.setChapterType(ch.getChapterType());
            cr.setDifficultyLevel(ch.getDifficultyLevel() != null ? ch.getDifficultyLevel() : 2);
            cr.setChapterOrder(ch.getChapterOrder());
            cr.setContentCount(ch.getContentCount() != null ? ch.getContentCount() : 0);
            cr.setQuestionCount(ch.getQuestionCount() != null ? ch.getQuestionCount() : 0);
            cr.setStudyProgressRate(BigDecimal.ZERO);
            cr.setStudyStatus(0);
            boolean unlock = unlockSourceIds.contains(ch.getId());
            cr.setUnlockStatus(unlock ? 1 : 0);
            if (unlock) {
                cr.setUnlockTime(now);
            }
            cr.setCreateTime(now);
            eduStudentCourseChapterRecordService.save(cr);
        }

        return record;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduStudentCourseRecord reportContentLearned(Long studentId, Long courseId, Long contentId) {
        if (studentId == null || courseId == null || contentId == null) {
            return null;
        }
        EduStudentCourseRecord record = eduStudentCourseRecordService.getByStudentIdAndCourseId(studentId, courseId);
        if (record == null) {
            return null;
        }
        Map<String, Integer> progress = parseContentProgress(record.getContentProgress());
        progress.put(String.valueOf(contentId), 1);
        record.setContentProgress(writeContentProgress(progress));
        record.setLastLearnTime(LocalDateTime.now());

        int totalContent = countCourseContentTotal(courseId);
        if (totalContent > 0) {
            int completed = progress.size();
            BigDecimal rate = BigDecimal.valueOf(completed * 100.0 / totalContent).setScale(2, RoundingMode.HALF_UP);
            if (rate.compareTo(BigDecimal.valueOf(100)) > 0) {
                rate = BigDecimal.valueOf(100);
            }
            record.setProgressRate(rate);
            if (rate.compareTo(BigDecimal.valueOf(100)) >= 0) {
                record.setLearnStatus(2);
                if (record.getFinishTime() == null) {
                    record.setFinishTime(LocalDateTime.now());
                }
            }
        }
        eduStudentCourseRecordService.updateById(record);
        updateChapterProgress(record.getId(), record.getContentProgress(), contentId);
        return record;
    }

    /** 更新该内容所属章节的学习进度（studyProgressRate、studyStatus） */
    private void updateChapterProgress(Long recordId, String contentProgressJson, Long contentId) {
        EduChapterContent content = eduChapterContentService.getById(contentId);
        if (content == null || content.getChapterId() == null) return;
        Long chapterId = content.getChapterId();
        EduStudentCourseChapterRecord query = new EduStudentCourseChapterRecord();
        query.setRecordId(recordId);
        query.setSourceChapterId(chapterId);
        List<EduStudentCourseChapterRecord> list = eduStudentCourseChapterRecordService.listQuery(query);
        if (list.isEmpty()) return;
        EduStudentCourseChapterRecord chapterRecord = list.get(0);
        EduChapterContent contentQuery = new EduChapterContent();
        contentQuery.setChapterId(chapterId);
        List<EduChapterContent> chapterContents = eduChapterContentService.listQuery(contentQuery);
        int total = chapterContents.size();
        if (total == 0) return;
        Map<String, Integer> progress = parseContentProgress(contentProgressJson);
        List<Long> chapterContentIds = chapterContents.stream().map(EduChapterContent::getId).collect(Collectors.toList());
        long completedCount = chapterContentIds.stream()
            .filter(id -> progress.containsKey(String.valueOf(id)))
            .count();
        BigDecimal rate = BigDecimal.valueOf(completedCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        if (rate.compareTo(BigDecimal.valueOf(100)) > 0) rate = BigDecimal.valueOf(100);
        chapterRecord.setStudyProgressRate(rate);
        chapterRecord.setLastLearnTime(LocalDateTime.now());
        if (rate.compareTo(BigDecimal.valueOf(100)) >= 0) {
            chapterRecord.setStudyStatus(2);
            if (chapterRecord.getFinishTime() == null) {
                chapterRecord.setFinishTime(LocalDateTime.now());
            }
            EduStudentCourseRecord record = eduStudentCourseRecordService.getById(recordId);
            if (record != null && record.getChapterTotalCount() != null && record.getChapterTotalCount() > 0) {
                refreshCourseProgress(recordId, record.getChapterTotalCount());
            }
        } else {
            chapterRecord.setStudyStatus(1);
        }
        eduStudentCourseChapterRecordService.updateById(chapterRecord);
    }

    private Map<String, Integer> parseContentProgress(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, Integer>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private String writeContentProgress(Map<String, Integer> progress) {
        if (progress == null || progress.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(progress);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** 统计该课程下所有章节的内容总数（用于计算学习进度分母） */
    private int countCourseContentTotal(Long courseId) {
        EduCourseChapter query = new EduCourseChapter();
        query.setCourseId(courseId);
        List<EduCourseChapter> chapters = eduCourseChapterService.listQuery(query);
        int total = 0;
        for (EduCourseChapter ch : chapters) {
            EduChapterContent cq = new EduChapterContent();
            cq.setChapterId(ch.getId());
            total += eduChapterContentService.listQuery(cq).size();
        }
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduStudentCourseChapterRecord unlockNextChapter(Long studentId, Long courseId, Long afterChapterId) {
        if (studentId == null || courseId == null || afterChapterId == null) return null;
        EduStudentCourseRecord record = eduStudentCourseRecordService.getByStudentIdAndCourseId(studentId, courseId);
        if (record == null) return null;
        EduStudentCourseChapterRecord crQuery = new EduStudentCourseChapterRecord();
        crQuery.setRecordId(record.getId());
        List<EduStudentCourseChapterRecord> list = eduStudentCourseChapterRecordService.listQuery(crQuery);
        if (list == null) list = new ArrayList<>();
        list = list.stream()
            .sorted(Comparator.comparing(EduStudentCourseChapterRecord::getChapterOrder, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (afterChapterId.equals(list.get(i).getSourceChapterId())) { idx = i; break; }
        }
        if (idx < 0 || idx + 1 >= list.size()) return null;
        EduStudentCourseChapterRecord next = list.get(idx + 1);
        if (next.getUnlockStatus() != null && next.getUnlockStatus() == 1) {
            refreshCourseProgress(record.getId(), record.getChapterTotalCount());
            return next;
        }
        next.setUnlockStatus(1);
        next.setUnlockTime(LocalDateTime.now());
        eduStudentCourseChapterRecordService.updateById(next);
        // 将当前通过测试的章节标记为已完成，并重新计算课程进度（x/8章 与 百分比）
        EduStudentCourseChapterRecord current = list.get(idx);
        if (current.getStudyStatus() == null || current.getStudyStatus() != 2) {
            current.setStudyStatus(2);
            current.setFinishTime(LocalDateTime.now());
            eduStudentCourseChapterRecordService.updateById(current);
        }
        refreshCourseProgress(record.getId(), record.getChapterTotalCount());
        return next;
    }

    /** 根据章节完成数重新计算并更新课程学习进度（finishedChapterCount = 内容学习100%的章节数） */
    private void refreshCourseProgress(Long recordId, Integer chapterTotalCount) {
        if (recordId == null || chapterTotalCount == null || chapterTotalCount <= 0) return;
        EduStudentCourseChapterRecord query = new EduStudentCourseChapterRecord();
        query.setRecordId(recordId);
        List<EduStudentCourseChapterRecord> list = eduStudentCourseChapterRecordService.listQuery(query);
        long finishedCount = list == null ? 0 : list.stream()
            .filter(c -> c.getStudyProgressRate() != null && c.getStudyProgressRate().intValue() >= 100)
            .count();
        EduStudentCourseRecord record = eduStudentCourseRecordService.getById(recordId);
        if (record == null) return;
        record.setFinishedChapterCount((int) finishedCount);
        eduStudentCourseRecordService.updateById(record);
    }

    private static final int UNLOCK_STATUS_UNLOCKED = 1;
    private static final int STUDY_STATUS_FINISHED = 2;

    @Override
    public List<Map<String, Object>> getDashboardOverview(Long studentId) {
        EduStudentCourseRecord query = new EduStudentCourseRecord();
        query.setStudentId(studentId);
        List<EduStudentCourseRecord> records = eduStudentCourseRecordService.listQuery(query);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (EduStudentCourseRecord record : records) {
            Long courseId = record.getCourseId();
            EduCourse course = eduCourseService.getById(courseId);
            String courseName = course != null ? course.getCourseName() : String.valueOf(courseId);

            EduStudentCourseChapterRecord chapterQuery = new EduStudentCourseChapterRecord();
            chapterQuery.setRecordId(record.getId());
            List<EduStudentCourseChapterRecord> chapters = eduStudentCourseChapterRecordService.listQuery(chapterQuery);
            if (chapters == null) chapters = new ArrayList<>();

            int totalChapters = record.getChapterTotalCount() != null ? record.getChapterTotalCount() : chapters.size();
            long unlockedCount = chapters.stream()
                .filter(c -> c.getUnlockStatus() != null && c.getUnlockStatus() == UNLOCK_STATUS_UNLOCKED)
                .count();
            // 已完成章节数 = 内容学习进度达 100% 的章节数（实时统计，不依赖库里的 finishedChapterCount）
            long finishedCount = chapters.stream()
                .filter(c -> c.getStudyProgressRate() != null && c.getStudyProgressRate().intValue() >= 100)
                .count();

            List<Map<String, Object>> todoUnlearned = new ArrayList<>();
            List<Map<String, Object>> todoUntested = new ArrayList<>();
            for (EduStudentCourseChapterRecord ch : chapters) {
                if (ch.getUnlockStatus() == null || ch.getUnlockStatus() != UNLOCK_STATUS_UNLOCKED) continue;
                int studyStatus = ch.getStudyStatus() != null ? ch.getStudyStatus() : 0;
                int progress = ch.getStudyProgressRate() != null ? ch.getStudyProgressRate().intValue() : 0;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("chapterId", ch.getSourceChapterId());
                item.put("chapterRecordId", ch.getId());
                item.put("chapterName", ch.getChapterName());
                item.put("courseId", courseId);
                item.put("courseName", courseName);
                if (progress < 100) {
                    todoUnlearned.add(item);
                } else if (studyStatus != STUDY_STATUS_FINISHED) {
                    todoUntested.add(item);
                }
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseId", courseId);
            row.put("courseName", courseName);
            row.put("cover", course != null ? course.getCover() : null);
            row.put("recordId", record.getId());
            row.put("unlockedChapterCount", (int) unlockedCount);
            row.put("totalChapterCount", totalChapters);
            row.put("finishedChapterCount", (int) finishedCount);
            row.put("progressRate", record.getProgressRate() != null ? record.getProgressRate() : BigDecimal.ZERO);
            row.put("todoUnlearned", todoUnlearned);
            row.put("todoUntested", todoUntested);
            result.add(row);
        }
        return result;
    }
}
