package com.example.exam_system.exammanage.dto;

import java.time.LocalDateTime;

public class ExamResponseDTO {
    private Long id;
    private String creatorId;
    private Long groupId;
    private Long paperId;
    private String examName;
    private LocalDateTime earliestStartTime;
    private LocalDateTime latestStartTime;
    private Integer durationMinutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public LocalDateTime getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(LocalDateTime earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public LocalDateTime getLatestStartTime() { return latestStartTime; }
    public void setLatestStartTime(LocalDateTime latestStartTime) { this.latestStartTime = latestStartTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
