package com.edu.teaching.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 章节测试及格线配置：简单70、中等60、困难50
 */
@Data
@Component
@ConfigurationProperties(prefix = "exam.pass-line")
public class ExamPassLineConfig {
    /** 简单 difficultyLevel=1 */
    private int easy = 70;
    /** 中等 difficultyLevel=2 */
    private int medium = 60;
    /** 困难 difficultyLevel=3 */
    private int hard = 50;

    public int getPassLineByDifficulty(Integer difficultyLevel) {
        if (difficultyLevel == null) return 60;
        switch (difficultyLevel) {
            case 1: return easy;
            case 2: return medium;
            case 3: return hard;
            default: return medium;
        }
    }
}
