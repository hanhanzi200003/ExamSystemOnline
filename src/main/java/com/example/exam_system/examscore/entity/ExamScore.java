package com.example.exam_system.examscore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_scores")
public class ExamScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private Long sessionId;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "exam_name", length = 200)
    private String examName;

    @Column(name = "group_name", length = 200)
    private String groupName;

    @Column(name = "teacher_name", length = 100)
    private String teacherName;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "score_percentage")
    private Double scorePercentage;

    @Column(name = "scheduled_duration_minutes")
    private Integer scheduledDurationMinutes;

    @Column(name = "earliest_start_time")
    private LocalDateTime earliestStartTime;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "exam_duration_minutes")
    private Integer examDurationMinutes;

    @Column(name = "total_score", nullable = false)
    private Double totalScore = 0.0;

    @Column(name = "objective_score", nullable = false)
    private Double objectiveScore = 0.0;

    @Column(name = "subjective_score", nullable = false)
    private Double subjectiveScore = 0.0;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ExamScore() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Double getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Double scorePercentage) { this.scorePercentage = scorePercentage; }

    public Integer getScheduledDurationMinutes() { return scheduledDurationMinutes; }
    public void setScheduledDurationMinutes(Integer scheduledDurationMinutes) { this.scheduledDurationMinutes = scheduledDurationMinutes; }

    public LocalDateTime getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(LocalDateTime earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getExamDurationMinutes() { return examDurationMinutes; }
    public void setExamDurationMinutes(Integer examDurationMinutes) { this.examDurationMinutes = examDurationMinutes; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }

    public Double getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(Double subjectiveScore) { this.subjectiveScore = subjectiveScore; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

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
