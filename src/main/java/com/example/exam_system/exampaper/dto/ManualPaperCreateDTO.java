package com.example.exam_system.exampaper.dto;

import lombok.Data;
import java.util.List;

@Data
public class ManualPaperCreateDTO {
    private String paperName;
    private List<QuestionSelectionDTO> questions;

    @Data
    public static class QuestionSelectionDTO {
        private Long questionId;
        private Double score;
    }
}
