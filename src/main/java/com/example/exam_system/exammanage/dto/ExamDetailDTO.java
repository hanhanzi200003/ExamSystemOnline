package com.example.exam_system.exammanage.dto;

import java.time.LocalDateTime;

public class ExamDetailDTO {
    private Long id;
    private Long groupId;
    private String creatorId;
    private String examName;
    private String description;
    private Long paperId;
    private LocalDateTime earliestStartTime;
    private LocalDateTime latestStartTime;
    private Integer durationMinutes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String teacherNickname;

    public ExamDetailDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public LocalDateTime getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(LocalDateTime earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public LocalDateTime getLatestStartTime() { return latestStartTime; }
    public void setLatestStartTime(LocalDateTime latestStartTime) { this.latestStartTime = latestStartTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getTeacherNickname() { return teacherNickname; }
    public void setTeacherNickname(String teacherNickname) { this.teacherNickname = teacherNickname; }
}
