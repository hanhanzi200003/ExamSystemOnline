package com.example.exam_system.exammanage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "exam_name", nullable = false, length = 200)
    private String examName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Column(name = "earliest_start_time", nullable = false)
    private LocalDateTime earliestStartTime;

    @Column(name = "latest_start_time", nullable = false)
    private LocalDateTime latestStartTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Exam() {}

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
