package com.edu.teaching.module.edu_exam_paper_question.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.teaching.module.edu_exam_paper_question.dto.AiGeneratePaperQuestionDTO;
import com.edu.teaching.module.edu_exam_paper_question.entity.EduExamPaperQuestion;
import com.edu.teaching.module.edu_exam_paper_question.mapper.EduExamPaperQuestionMapper;
import com.edu.teaching.module.edu_exam_paper_question.service.EduExamPaperQuestionService;
import com.edu.teaching.util.AiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.teaching.vo.SelectVO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wuming
 * @description: 试卷题目关联Service实现类
 */
@Slf4j
@Service
public class EduExamPaperQuestionServiceImpl extends ServiceImpl<EduExamPaperQuestionMapper, EduExamPaperQuestion> implements EduExamPaperQuestionService {

    @Autowired
    private EduExamPaperQuestionMapper eduExamPaperQuestionMapper;

    @Autowired
    private AiChatClient aiChatClient;

    @Override
    public IPage<EduExamPaperQuestion> pageQuery(Integer current, Integer size, EduExamPaperQuestion entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<EduExamPaperQuestion> records = eduExamPaperQuestionMapper.pageQuery(entity, offset, limit);
        Long total = eduExamPaperQuestionMapper.countQuery(entity);

        Page<EduExamPaperQuestion> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<EduExamPaperQuestion> listQuery(EduExamPaperQuestion entity) {
        return eduExamPaperQuestionMapper.listQuery(entity);
    }

    @Override
    public EduExamPaperQuestion getById(Long id) {
        return eduExamPaperQuestionMapper.getById(id);
    }

    /**
     * 下拉数据：edu_chapter_exam_paper
     */
    @Override
    public List<SelectVO> listEduChapterExamPaper() {
        return eduExamPaperQuestionMapper.listEduChapterExamPaper();
    }

    /**
     * 下拉数据：edu_question_bank
     */
    @Override
    public List<SelectVO> listEduQuestionBank() {
        return eduExamPaperQuestionMapper.listEduQuestionBank();
    }

    /**
     * AI生成试卷题目
     */
    @Override
    public List<Long> aiGeneratePaperQuestions(Long paperId, Integer difficultyLevel, Integer questionCount, List<AiGeneratePaperQuestionDTO.AvailableQuestion> availableQuestions) {
        if (availableQuestions == null || availableQuestions.isEmpty()) {
            log.warn("可用题目列表为空，无法生成试卷");
            return new ArrayList<>();
        }

        // 根据试卷难度筛选合适的题目
        // 难度映射：试卷难度1(低) -> 题目难度1(简单), 试卷难度2(中) -> 题目难度2(中等), 试卷难度3(高) -> 题目难度3(困难)
        Integer targetDifficulty = difficultyLevel; // 试卷难度和题目难度对应
        
        // 筛选符合难度的题目
        List<AiGeneratePaperQuestionDTO.AvailableQuestion> filteredQuestions = availableQuestions.stream()
                .filter(q -> q.getDifficulty() != null && q.getDifficulty().equals(targetDifficulty))
                .collect(Collectors.toList());

        // 如果筛选后题目不足，则放宽条件，允许相邻难度的题目
        if (filteredQuestions.size() < questionCount) {
            if (targetDifficulty == 1) {
                // 低难度试卷：优先简单，其次中等
                filteredQuestions = availableQuestions.stream()
                        .filter(q -> q.getDifficulty() != null && (q.getDifficulty() == 1 || q.getDifficulty() == 2))
                        .collect(Collectors.toList());
            } else if (targetDifficulty == 2) {
                // 中等难度试卷：优先中等，其次简单或困难
                filteredQuestions = availableQuestions.stream()
                        .filter(q -> q.getDifficulty() != null && (q.getDifficulty() == 2 || q.getDifficulty() == 1 || q.getDifficulty() == 3))
                        .collect(Collectors.toList());
            } else if (targetDifficulty == 3) {
                // 高难度试卷：优先困难，其次中等
                filteredQuestions = availableQuestions.stream()
                        .filter(q -> q.getDifficulty() != null && (q.getDifficulty() == 3 || q.getDifficulty() == 2))
                        .collect(Collectors.toList());
            }
        }

        // 如果仍然不足，使用所有题目
        if (filteredQuestions.size() < questionCount) {
            filteredQuestions = availableQuestions;
        }

        // 构建AI提示词：明确题目ID、题型、难度，并要求题型均匀分布
        String difficultyName = getDifficultyName(difficultyLevel);
        StringBuilder prompt = new StringBuilder();
        prompt.append("请从以下可用题目中选出 exactly ").append(questionCount).append(" 道题目组成试卷。\n\n");
        prompt.append("【试卷要求】\n");
        prompt.append("- 试卷难度要求：").append(difficultyName).append("\n");
        prompt.append("- 需要选择的题目数量：").append(questionCount).append(" 道（不能多也不能少）\n");
        prompt.append("- 题型均匀分布：请让单选题、多选题、填空题、判断题、主观题五种题型的数量尽量均衡。若某种题型题目不足，其余题型可适当多选，但整体上各题型数量不要悬殊。\n\n");
        prompt.append("【可用题目列表】每行格式为：题目ID、题型、难度、题目标题、分值。\n");
        
        for (int i = 0; i < filteredQuestions.size(); i++) {
            AiGeneratePaperQuestionDTO.AvailableQuestion q = filteredQuestions.get(i);
            String typeName = getQuestionTypeName(q.getType());
            String diffName = getDifficultyName(q.getDifficulty());
            prompt.append(i + 1).append(". 题目ID:").append(q.getId())
                  .append(" | 题型:").append(typeName)
                  .append(" | 难度:").append(diffName)
                  .append(" | 标题:").append(q.getTitle() != null ? q.getTitle() : "")
                  .append(" | 分值:").append(q.getScore()).append("\n");
        }
        
        prompt.append("\n【输出要求】\n");
        prompt.append("请根据上述要求选出 ").append(questionCount).append(" 道题目，并保证题型尽量均匀分布。\n");
        prompt.append("只返回选中题目的 题目ID，用英文逗号分隔，例如：123,456,789\n");
        prompt.append("不要返回任何其他文字、序号或解释，只返回ID列表。");

        // 调用AI
        String systemPrompt = "你是一个专业的试卷组卷助手。你需要根据试卷难度要求选题，并保证选题结果中单选题、多选题、填空题、判断题、主观题的数量尽量均匀分布。";
        String aiResponse = aiChatClient.chat(systemPrompt, prompt.toString());

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.error("AI返回结果为空");
            // 如果AI调用失败，使用简单策略：随机选择符合难度的题目
            return selectQuestionsBySimpleStrategy(filteredQuestions, questionCount);
        }

        // 解析AI返回的ID列表
        List<Long> selectedIds = parseQuestionIds(aiResponse);
        
        // 验证选中的ID是否都在可用题目列表中
        List<Long> availableIds = filteredQuestions.stream()
                .map(AiGeneratePaperQuestionDTO.AvailableQuestion::getId)
                .collect(Collectors.toList());
        
        selectedIds = selectedIds.stream()
                .filter(availableIds::contains)
                .collect(Collectors.toList());

        // 如果解析失败或数量不足，使用简单策略
        if (selectedIds.size() < questionCount) {
            log.warn("AI返回的题目数量不足，使用简单策略补充");
            List<Long> simpleSelected = selectQuestionsBySimpleStrategy(filteredQuestions, questionCount);
            // 合并结果，去重
            selectedIds.addAll(simpleSelected);
            selectedIds = selectedIds.stream().distinct().limit(questionCount).collect(Collectors.toList());
        }

        return selectedIds.stream().limit(questionCount).collect(Collectors.toList());
    }

    /**
     * 简单策略：根据难度优先选择题目
     */
    private List<Long> selectQuestionsBySimpleStrategy(List<AiGeneratePaperQuestionDTO.AvailableQuestion> questions, int count) {
        return questions.stream()
                .limit(count)
                .map(AiGeneratePaperQuestionDTO.AvailableQuestion::getId)
                .collect(Collectors.toList());
    }

    /**
     * 解析AI返回的题目ID列表
     */
    private List<Long> parseQuestionIds(String aiResponse) {
        List<Long> ids = new ArrayList<>();
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return ids;
        }

        // 提取所有数字ID
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(aiResponse);
        while (matcher.find()) {
            try {
                Long id = Long.parseLong(matcher.group());
                ids.add(id);
            } catch (NumberFormatException e) {
                log.warn("解析ID失败: {}", matcher.group());
            }
        }

        return ids;
    }

    /**
     * 获取难度名称
     */
    private String getDifficultyName(Integer difficulty) {
        if (difficulty == null) {
            return "未知";
        }
        switch (difficulty) {
            case 1:
                return "简单/低";
            case 2:
                return "中等/中";
            case 3:
                return "困难/高";
            default:
                return "未知";
        }
    }

    /**
     * 获取题目类型名称
     */
    private String getQuestionTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "单选题";
            case 2:
                return "多选题";
            case 3:
                return "判断题";
            case 4:
                return "填空题";
            case 5:
                return "主观题";
            default:
                return "未知";
        }
    }

}
