package com.edu.teaching.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 题目解析器
 */
public class ExcelQuestionParser {

    /**
     * 解析 Excel 文件，返回 Map 列表（不依赖实体类）
     */
    public static List<java.util.Map<String, Object>> parse(byte[] bytes) throws IOException {
        List<java.util.Map<String, Object>> questions = new ArrayList<>();

        // 创建 Workbook
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes));

        // 获取第一个工作表
        Sheet sheet = workbook.getSheetAt(0);

        // 从第二行开始读取数据（第一行是表头）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            java.util.Map<String, Object> question = new java.util.HashMap<>();

            // 读取数据
            String questionTitle = getCellValue(row.getCell(0));
            String questionTypeStr = getCellValue(row.getCell(1));
            String difficultyLevelStr = getCellValue(row.getCell(2));
            String answerContent = getCellValue(row.getCell(3));
            String analysisContent = getCellValue(row.getCell(4));
            String scoreStr = getCellValue(row.getCell(5));

            // 跳过空行
            if (questionTitle == null || questionTitle.trim().isEmpty()) {
                continue;
            }

            // 使用数据库字段名（下划线命名）
            question.put("question_title", questionTitle);
            question.put("question_type", mapQuestionType(questionTypeStr));
            question.put("difficulty_level", mapDifficultyLevel(difficultyLevelStr));
            question.put("answer_content", answerContent);
            question.put("analysis_content", analysisContent);
            question.put("score", scoreStr != null && !scoreStr.isEmpty() ? Integer.parseInt(scoreStr) : 5);
            question.put("status", 1); // 默认启用

            questions.add(question);
        }

        workbook.close();
        return questions;
    }

    /**
     * 获取单元格的值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();
        if (cellType == null) {
            return null;
        }

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 处理数字类型
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * 映射题目类型
     */
    private static Integer mapQuestionType(String typeStr) {
        if (typeStr == null) return 1;

        String lowerType = typeStr.toLowerCase();

        if (lowerType.contains("单选")) return 1;
        if (lowerType.contains("多选")) return 2;
        if (lowerType.contains("判断")) return 3;
        if (lowerType.contains("填空")) return 4;
        if (lowerType.contains("主观")) return 5;

        try {
            int type = Integer.parseInt(typeStr);
            if (type >= 1 && type <= 5) return type;
        } catch (NumberFormatException e) {
            // 忽略
        }

        return 1;
    }

    /**
     * 映射难度等级
     */
    private static Integer mapDifficultyLevel(String levelStr) {
        if (levelStr == null) return 2;

        String lowerLevel = levelStr.toLowerCase();

        if (lowerLevel.contains("简单") || lowerLevel.contains("低")) return 1;
        if (lowerLevel.contains("中等") || lowerLevel.contains("中")) return 2;
        if (lowerLevel.contains("困难") || lowerLevel.contains("高")) return 3;

        try {
            int level = Integer.parseInt(levelStr);
            if (level >= 1 && level <= 3) return level;
        } catch (NumberFormatException e) {
            // 忽略
        }

        return 2;
    }
}