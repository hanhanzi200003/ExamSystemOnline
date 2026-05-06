package com.example.exam_system.exammanage.dto;

import java.time.LocalDateTime;

public class ExamCreateManualDTO {
    private Long groupId;
    private Long paperId;
    private String examName;
    private String description;
    private LocalDateTime earliestStartTime;
    private LocalDateTime latestStartTime;
    private Integer durationMinutes;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

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
}
