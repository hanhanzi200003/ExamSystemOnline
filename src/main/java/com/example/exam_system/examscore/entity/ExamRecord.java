package com.example.exam_system.examscore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_records")
public class ExamRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "teacher_id", nullable = false, length = 50)
    private String teacherId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Column(name = "exam_name", nullable = false, length = 200)
    private String examName;

    @Column(name = "group_name", length = 200)
    private String groupName;

    @Column(name = "teacher_name", length = 100)
    private String teacherName;

    @Column(name = "earliest_start_time")
    private LocalDateTime earliestStartTime;

    @Column(name = "session_id", nullable = false, unique = true)
    private Long sessionId;

    @Column(name = "total_score", nullable = false)
    private Double totalScore = 0.0;

    @Column(name = "objective_score", nullable = false)
    private Double objectiveScore = 0.0;

    @Column(name = "subjective_score", nullable = false)
    private Double subjectiveScore = 0.0;

    @Column(name = "max_score", nullable = false)
    private Double maxScore = 0.0;

    @Column(name = "score_percentage")
    private Double scorePercentage;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer actualDurationMinutes;

    @Column(name = "scheduled_duration_minutes")
    private Integer scheduledDurationMinutes;

    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "is_submitted", nullable = false)
    private Boolean isSubmitted = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RecordStatus {
        NOT_STARTED,      // 未开始
        ONGOING,          // 进行中
        SUBMITTED,        // 已提交
        GRADED,           // 已批改
        OVERDUE           // 已过期
    }

    public ExamRecord() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public LocalDateTime getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(LocalDateTime earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }

    public Double getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(Double subjectiveScore) { this.subjectiveScore = subjectiveScore; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Double getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Double scorePercentage) { this.scorePercentage = scorePercentage; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }

    public Integer getScheduledDurationMinutes() { return scheduledDurationMinutes; }
    public void setScheduledDurationMinutes(Integer scheduledDurationMinutes) { this.scheduledDurationMinutes = scheduledDurationMinutes; }

    public RecordStatus getStatus() { return status; }
    public void setStatus(RecordStatus status) { this.status = status; }

    public Boolean getIsSubmitted() { return isSubmitted; }
    public void setIsSubmitted(Boolean isSubmitted) { this.isSubmitted = isSubmitted; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

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
