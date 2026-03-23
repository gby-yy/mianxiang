package com.edu.teaching.basic.service.impl;

import com.edu.teaching.basic.service.StudentChapterExamService;
import com.edu.teaching.basic.service.SubjectiveScoreService;
import com.edu.teaching.config.ExamPassLineConfig;
import com.edu.teaching.module.edu_chapter_exam_paper.entity.EduChapterExamPaper;
import com.edu.teaching.module.edu_chapter_exam_paper.service.EduChapterExamPaperService;
import com.edu.teaching.module.edu_exam_paper_question.entity.EduExamPaperQuestion;
import com.edu.teaching.module.edu_exam_paper_question.service.EduExamPaperQuestionService;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import com.edu.teaching.module.edu_question_bank.service.EduQuestionBankService;
import com.edu.teaching.module.edu_question_option.entity.EduQuestionOption;
import com.edu.teaching.module.edu_question_option.service.EduQuestionOptionService;
import com.edu.teaching.module.edu_student_exam_answer.entity.EduStudentExamAnswer;
import com.edu.teaching.module.edu_student_exam_answer.service.EduStudentExamAnswerService;
import com.edu.teaching.module.edu_student_exam_record.entity.EduStudentExamRecord;
import com.edu.teaching.module.edu_student_exam_record.service.EduStudentExamRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生章节测试：按难度获取试卷、提交阅卷（单选/多选/判断/填空/主观题，主观题AI判分）
 */
@Slf4j
@Service
public class StudentChapterExamServiceImpl implements StudentChapterExamService {

    @Autowired
    private EduChapterExamPaperService eduChapterExamPaperService;
    @Autowired
    private EduExamPaperQuestionService eduExamPaperQuestionService;
    @Autowired
    private EduQuestionBankService eduQuestionBankService;
    @Autowired
    private EduQuestionOptionService eduQuestionOptionService;
    @Autowired
    private EduStudentExamRecordService eduStudentExamRecordService;
    @Autowired
    private EduStudentExamAnswerService eduStudentExamAnswerService;
    @Autowired
    private SubjectiveScoreService subjectiveScoreService;
    @Autowired
    private ExamPassLineConfig examPassLineConfig;

    @Override
    public Map<String, Object> getChapterPaperByDifficulty(Long chapterId, Integer difficultyLevel) {
        EduChapterExamPaper query = new EduChapterExamPaper();
        query.setChapterId(chapterId);
        query.setDifficultyLevel(difficultyLevel);
        query.setStatus(1);
        List<EduChapterExamPaper> list = eduChapterExamPaperService.listQuery(query);
        if (list == null || list.isEmpty()) return null;
        // 多张符合要求的试卷时随机选一张
        EduChapterExamPaper paper = list.size() == 1 ? list.get(0) : list.get(new Random().nextInt(list.size()));

        EduExamPaperQuestion pqQuery = new EduExamPaperQuestion();
        pqQuery.setPaperId(paper.getId());
        List<EduExamPaperQuestion> pqList = eduExamPaperQuestionService.listQuery(pqQuery);
        if (pqList == null || pqList.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("paper", toPaperMap(paper));
            result.put("questions", Collections.emptyList());
            return result;
        }

        List<Map<String, Object>> questions = new ArrayList<>();
        for (EduExamPaperQuestion pq : pqList) {
            EduQuestionBank qb = eduQuestionBankService.getById(pq.getQuestionId());
            if (qb == null) continue;
            EduQuestionOption optQuery = new EduQuestionOption();
            optQuery.setQuestionId(pq.getQuestionId());
            List<EduQuestionOption> options = eduQuestionOptionService.listQuery(optQuery);
            options = options != null ? options.stream()
                .sorted(Comparator.comparing(EduQuestionOption::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList()) : Collections.emptyList();

            Map<String, Object> q = new HashMap<>();
            q.put("questionId", pq.getQuestionId());
            q.put("questionTitle", qb.getQuestionTitle());
            q.put("questionType", qb.getQuestionType());
            q.put("questionScore", pq.getQuestionScore());
            q.put("standardAnswer", qb.getAnswerContent());
            List<Map<String, String>> optMaps = options.stream().map(o -> {
                Map<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(o.getId()));
                m.put("optionKey", o.getOptionLabel());
                m.put("optionContent", o.getOptionContent());
                return m;
            }).collect(Collectors.toList());
            q.put("options", optMaps);
            questions.add(q);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paper", toPaperMap(paper));
        result.put("questions", questions);
        return result;
    }

    @Override
    public Map<String, Object> getPaperDetail(Long paperId) {
        if (paperId == null) return null;
        EduChapterExamPaper paper = eduChapterExamPaperService.getById(paperId);
        if (paper == null) return null;
        EduExamPaperQuestion pqQuery = new EduExamPaperQuestion();
        pqQuery.setPaperId(paperId);
        List<EduExamPaperQuestion> pqList = eduExamPaperQuestionService.listQuery(pqQuery);
        if (pqList == null || pqList.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("paper", toPaperMap(paper));
            result.put("questions", Collections.emptyList());
            return result;
        }
        List<Map<String, Object>> questions = new ArrayList<>();
        for (EduExamPaperQuestion pq : pqList) {
            EduQuestionBank qb = eduQuestionBankService.getById(pq.getQuestionId());
            if (qb == null) continue;
            EduQuestionOption optQuery = new EduQuestionOption();
            optQuery.setQuestionId(pq.getQuestionId());
            List<EduQuestionOption> options = eduQuestionOptionService.listQuery(optQuery);
            options = options != null ? options.stream()
                .sorted(Comparator.comparing(EduQuestionOption::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList()) : Collections.emptyList();
            Map<String, Object> q = new HashMap<>();
            q.put("questionId", pq.getQuestionId());
            q.put("questionTitle", qb.getQuestionTitle());
            q.put("questionType", qb.getQuestionType());
            q.put("questionScore", pq.getQuestionScore());
            q.put("standardAnswer", qb.getAnswerContent());
            List<Map<String, String>> optMaps = options.stream().map(o -> {
                Map<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(o.getId()));
                m.put("optionKey", o.getOptionLabel());
                m.put("optionContent", o.getOptionContent());
                return m;
            }).collect(Collectors.toList());
            q.put("options", optMaps);
            questions.add(q);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("paper", toPaperMap(paper));
        result.put("questions", questions);
        return result;
    }

    private Map<String, Object> toPaperMap(EduChapterExamPaper paper) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", paper.getId());
        m.put("paperName", paper.getPaperName());
        m.put("totalScore", paper.getTotalScore());
        m.put("durationMinutes", paper.getDurationMinutes());
        m.put("difficultyLevel", paper.getDifficultyLevel());
        m.put("questionCount", paper.getQuestionCount());
        return m;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitChapterExam(Long studentId, Long paperId, int useTimeSec, List<Map<String, Object>> answers) {
        EduChapterExamPaper paper = eduChapterExamPaperService.getById(paperId);
        if (paper == null) throw new com.edu.teaching.common.BusinessException("试卷不存在");
        int passLine = examPassLineConfig.getPassLineByDifficulty(paper.getDifficultyLevel());
        BigDecimal paperTotal = paper.getTotalScore() != null ? paper.getTotalScore() : BigDecimal.ZERO;

        EduExamPaperQuestion pqQuery = new EduExamPaperQuestion();
        pqQuery.setPaperId(paperId);
        List<EduExamPaperQuestion> pqList = eduExamPaperQuestionService.listQuery(pqQuery);
        Map<Long, String> userAnswerMap = new HashMap<>();
        if (answers != null) {
            for (Map<String, Object> a : answers) {
                Object qid = a.get("questionId");
                Object ans = a.get("answer");
                if (qid != null && ans != null)
                    userAnswerMap.put(qid instanceof Number ? ((Number) qid).longValue() : Long.parseLong(String.valueOf(qid)), String.valueOf(ans).trim());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        EduStudentExamRecord record = new EduStudentExamRecord();
        record.setExamNo("ER" + System.currentTimeMillis());
        record.setPaperId(paperId);
        record.setStudentId(studentId);
        record.setSubmitStatus(1);
        record.setStartTime(now);
        record.setSubmitTime(now);
        record.setDurationSeconds(useTimeSec);
        record.setCreateTime(now);
        record.setObjectiveScore(BigDecimal.ZERO);
        record.setSubjectiveScore(BigDecimal.ZERO);
        record.setTotalScore(BigDecimal.ZERO);
        eduStudentExamRecordService.save(record);
        Long recordId = record.getId();

        BigDecimal objectiveSum = BigDecimal.ZERO;
        BigDecimal subjectiveSum = BigDecimal.ZERO;
        int correctCount = 0;
        int wrongCount = 0;

        for (EduExamPaperQuestion pq : pqList) {
            Long questionId = pq.getQuestionId();
            EduQuestionBank qb = eduQuestionBankService.getById(questionId);
            if (qb == null) continue;
            String userAnswer = userAnswerMap.getOrDefault(questionId, "");
            EduQuestionOption optQuery = new EduQuestionOption();
            optQuery.setQuestionId(questionId);
            List<EduQuestionOption> options = eduQuestionOptionService.listQuery(optQuery);
            options = options != null ? options.stream()
                .sorted(Comparator.comparing(EduQuestionOption::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList()) : Collections.emptyList();

            int qType = qb.getQuestionType() != null ? qb.getQuestionType() : 1;
            String standardAnswer = qb.getAnswerContent() != null ? qb.getAnswerContent().trim() : "";
            BigDecimal questionScore = pq.getQuestionScore() != null ? pq.getQuestionScore() : BigDecimal.ZERO;
            boolean correct = false;
            BigDecimal score = BigDecimal.ZERO;

            if (qType == 1) {
                String std = (standardAnswer != null ? standardAnswer : "").trim().toUpperCase();
                String user = (userAnswer != null ? userAnswer : "").trim().toUpperCase();
                correct = std.equals(user);
                score = correct ? questionScore : BigDecimal.ZERO;
                objectiveSum = objectiveSum.add(score);
            } else if (qType == 2) {
                String std = normalizeMultiChoice(standardAnswer);
                String user = normalizeMultiChoice(userAnswer);
                correct = std.equals(user);
                score = correct ? questionScore : BigDecimal.ZERO;
                objectiveSum = objectiveSum.add(score);
            } else if (qType == 3) {
                String std = normalizeForJudge(standardAnswer, 3);
                String user = normalizeForJudge(userAnswer, 3);
                correct = std.equals(user);
                score = correct ? questionScore : BigDecimal.ZERO;
                objectiveSum = objectiveSum.add(score);
            } else if (qType == 4) {
                if (options != null && !options.isEmpty()) {
                    String[] userParts = userAnswer.split("[,\\s，、;；]+");
                    boolean allCorrect = userParts.length >= options.size();
                    if (allCorrect) {
                        for (int i = 0; i < options.size(); i++) {
                            String stdBlank = normalizeBlank(options.get(i).getOptionContent());
                            String userBlank = i < userParts.length ? normalizeBlank(userParts[i].trim()) : "";
                            if (!stdBlank.equals(userBlank)) { allCorrect = false; break; }
                        }
                    } else {
                        allCorrect = false;
                    }
                    correct = allCorrect;
                } else {
                    correct = normalizeBlank(standardAnswer).equals(normalizeBlank(userAnswer));
                }
                score = correct ? questionScore : BigDecimal.ZERO;
                objectiveSum = objectiveSum.add(score);
            } else if (qType == 5) {
                Map<String, Object> aiResult = subjectiveScoreService.gradeSubjective(
                    qb.getQuestionTitle(), userAnswer, standardAnswer);
                int aiScore = aiResult.get("score") != null ? ((Number) aiResult.get("score")).intValue() : 0;
                score = questionScore.multiply(BigDecimal.valueOf(aiScore)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                correct = (aiScore >= 60);
                subjectiveSum = subjectiveSum.add(score);
            } else {
                objectiveSum = objectiveSum.add(BigDecimal.ZERO);
            }

            if (correct) correctCount++; else wrongCount++;

            EduStudentExamAnswer ans = new EduStudentExamAnswer();
            ans.setExamRecordId(recordId);
            ans.setQuestionId(questionId);
            ans.setStudentAnswer(userAnswer);
            ans.setCorrectAnswer(standardAnswer);
            ans.setIsCorrect(correct ? 1 : 0);
            ans.setScore(score);
            ans.setAiScoreStatus(qType == 5 ? 1 : 0);
            ans.setCreateTime(now);
            eduStudentExamAnswerService.save(ans);
        }

        BigDecimal total = objectiveSum.add(subjectiveSum);
        record.setObjectiveScore(objectiveSum);
        record.setSubjectiveScore(subjectiveSum);
        record.setTotalScore(total);
        if (paperTotal.compareTo(BigDecimal.ZERO) > 0)
            record.setCorrectRate(total.divide(paperTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        eduStudentExamRecordService.updateById(record);

        boolean passed = total.compareTo(BigDecimal.valueOf(passLine)) >= 0;
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", total);
        result.put("paperTotalScore", paperTotal);
        result.put("passLine", passLine);
        result.put("passed", passed);
        result.put("correctCount", correctCount);
        result.put("wrongCount", wrongCount);
        result.put("recordId", recordId);
        return result;
    }

    private static String normalizeMultiChoice(String val) {
        if (val == null) return "";
        String s = val.trim().toUpperCase();
        Set<String> set = new LinkedHashSet<>();
        for (String part : s.split("[\\s,，、;；.．/／]+")) {
            String p = part.trim();
            for (int i = 0; i < p.length(); i++) {
                if (Character.isLetter(p.charAt(i))) set.add(String.valueOf(p.charAt(i)));
            }
        }
        return set.stream().sorted().collect(Collectors.joining(","));
    }

    private static String normalizeForJudge(String val, int questionType) {
        if (val == null) return "";
        String s = val.trim().toUpperCase().replaceAll("\\s+", "");
        if (questionType == 3) {
            if ("正确".equals(s) || "对".equals(s)) return "正确";
            if ("错误".equals(s) || "错".equals(s)) return "错误";
        }
        return s;
    }

    private static String normalizeBlank(String val) {
        if (val == null) return "";
        return val.trim().toUpperCase().replaceAll("\\s+", " ");
    }
}
