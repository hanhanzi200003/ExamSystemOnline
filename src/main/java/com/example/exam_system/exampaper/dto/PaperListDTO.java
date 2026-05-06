package com.example.exam_system.exampaper.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaperListDTO {
    private Long id;
    private String paperName;
    private Double totalScore;
    private Integer durationMinutes;
    private String paperType;
    private LocalDateTime createdAt;
}
