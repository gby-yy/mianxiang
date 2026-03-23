package com.edu.teaching.util;

import cn.hutool.core.util.StrUtil;
import com.edu.teaching.module.edu_question_bank.entity.EduQuestionBank;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 题目解析工具类
 */
@Slf4j
public class ExcelQuestionParser {

    /**
     * 解析 Excel 文件并返回题目列表
     *
     * Excel 模板格式：
     * | 题目标题 | 题目类型 | 难度等级 | 答案内容 | 解析 | 分值 |
     * |---------|---------|---------|---------|------|------|
     */
    public static List<EduQuestionBank> parse(byte[] excelBytes) throws IOException {
        List<EduQuestionBank> questions = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            // 跳过表头（第0行）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                EduQuestionBank question = parseRow(row, rowIndex + 1);
                if (question != null) {
                    questions.add(question);
                }
            }
        }

        log.info("Excel 解析完成，共解析出 {} 道题目", questions.size());
        return questions;
    }

    /**
     * 解析单行数据
     */
    private static EduQuestionBank parseRow(Row row, int rowNum) {
        EduQuestionBank question = new EduQuestionBank();

        try {
            // 题目标题 (第0列)
            String questionTitle = getCellValueAsString(row.getCell(0));
            if (StrUtil.isBlank(questionTitle)) {
                log.warn("第 {} 行：题目标题为空，跳过", rowNum);
                return null;
            }
            question.setQuestionTitle(questionTitle);

            // 题目类型 (第1列) - 1:单选 2:多选 3:判断 4:填空 5:主观
            String typeStr = getCellValueAsString(row.getCell(1));
            question.setQuestionType(parseQuestionType(typeStr));

            // 难度等级 (第2列) - 1:简单 2:中等 3:困难
            String difficultyStr = getCellValueAsString(row.getCell(2));
            question.setDifficultyLevel(parseDifficultyLevel(difficultyStr));

            // 答案内容 (第3列)
            question.setAnswerContent(getCellValueAsString(row.getCell(3)));

            // 解析 (第4列)
            question.setAnalysisContent(getCellValueAsString(row.getCell(4)));

            // 分值 (第5列)
            String scoreStr = getCellValueAsString(row.getCell(5));
            question.setScore(StrUtil.isNotBlank(scoreStr) ? new BigDecimal(scoreStr) : BigDecimal.ZERO);

            // 状态默认为启用
            question.setStatus(1);
            question.setCreateTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("解析第 {} 行失败: {}", rowNum, e.getMessage());
            return null;
        }

        return question;
    }

    /**
     * 获取单元格的字符串值
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    double num = cell.getNumericCellValue();
                    // 如果是整数，去掉小数点
                    yield num == (int) num ? String.valueOf((int) num) : String.valueOf(num);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }

    /**
     * 解析题目类型
     */
    private static Integer parseQuestionType(String typeStr) {
        if (StrUtil.isBlank(typeStr)) return 1; // 默认单选

        return switch (typeStr.toLowerCase()) {
            case "单选", "1", "单选题" -> 1;
            case "多选", "2", "多选题" -> 2;
            case "判断", "3", "判断题" -> 3;
            case "填空", "4", "填空题" -> 4;
            case "主观", "5", "主观题", "简答" -> 5;
            default -> {
                log.warn("未知的题目类型: {}，使用默认值(单选)", typeStr);
                yield 1;
            }
        };
    }

    /**
     * 解析难度等级
     */
    private static Integer parseDifficultyLevel(String difficultyStr) {
        if (StrUtil.isBlank(difficultyStr)) return 2; // 默认中等

        return switch (difficultyStr.toLowerCase()) {
            case "简单", "1", "easy" -> 1;
            case "中等", "2", "medium" -> 2;
            case "困难", "3", "hard" -> 3;
            default -> {
                log.warn("未知的难度等级: {}，使用默认值(中等)", difficultyStr);
                yield 2;
            }
        };
    }
}