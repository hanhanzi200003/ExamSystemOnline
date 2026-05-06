// src/main/java/com/example/exam_system/examquestionbank/dto/QuestionCreateDTO.java
package com.example.exam_system.examquestionbank.dto;

import com.example.exam_system.examquestionbank.enums.QuestionType;
import lombok.Data;
import java.util.List;

@Data
public class QuestionCreateDTO {
    private String content; // 题干
    private QuestionType questionType; // 题型

    // 选项列表（仅用于单选题和多选题）
    private List<OptionDTO> options;

    // 答案信息
    private String correctAnswer; // 正确答案
    private String analysis; // 解析

    @Data
    public static class OptionDTO {
        private String optionLabel; // A/B/C/D
        private String optionContent; // 选项内容
        private Boolean isCorrect; // 是否正确
    }
}
