package com.example.exam_system.exampaper.dto;

import lombok.Data;
import java.util.List;

@Data
public class AutoPaperCreateDTO {
    private String paperName;
    private List<TypeConfigDTO> typeConfigs;

    @Data
    public static class TypeConfigDTO {
        private String questionType;
        private Integer count;
        private Double scorePerQuestion;
    }
}
