package com.example.exam_system.exampaper.dto;

import com.example.exam_system.exampaper.enums.PaperType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperDetailDTO {
    private Long id;
    private String paperName;
    private String creatorId;
    private Double totalScore;
    private Integer durationMinutes;
    private String paperType;
    private LocalDateTime createdAt;
    private List<PaperQuestionDTO> questions;
    private String warningMessage;

    @Data
    public static class PaperQuestionDTO {
        private Long id;
        private Integer questionNumber;
        private Double score;
        private String questionType;
        private String questionContent;
        private List<OptionDTO> options;
        private String correctAnswer;
        private String analysis;

        @Data
        public static class OptionDTO {
            private String optionLabel;
            private String optionContent;
            private Boolean isCorrect;
        }
    }
}
