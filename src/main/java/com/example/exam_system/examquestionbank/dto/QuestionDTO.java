// src/main/java/com/example/exam_system/examquestionbank/dto/QuestionDTO.java
package com.example.exam_system.examquestionbank.dto;

import com.example.exam_system.examquestionbank.enums.QuestionType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private String content;
    private QuestionType questionType;
    private String creatorId;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 选项列表
    private List<OptionDTO> options;

    // 答案信息
    private String correctAnswer;
    private String analysis;

    @Data
    public static class OptionDTO {
        private Long id;
        private String optionLabel;
        private String optionContent;
        private Boolean isCorrect;
    }
}
