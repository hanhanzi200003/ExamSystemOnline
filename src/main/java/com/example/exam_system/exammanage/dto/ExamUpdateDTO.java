package com.example.exam_system.exammanage.dto;

import java.time.LocalDateTime;

public class ExamUpdateDTO {
    private String examName;
    private String description;
    private LocalDateTime earliestStartTime;
    private LocalDateTime latestStartTime;
    private Integer durationMinutes;
    private Long paperId;

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(LocalDateTime earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public LocalDateTime getLatestStartTime() { return latestStartTime; }
    public void setLatestStartTime(LocalDateTime latestStartTime) { this.latestStartTime = latestStartTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
}
