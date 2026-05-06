package com.example.exam_system.examsession.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_sessions")
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "session_token", unique = true, nullable = false, length = 64)
    private String sessionToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "expected_end_time", nullable = false)
    private LocalDateTime expectedEndTime;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "obtained_score", nullable = false)
    private Double obtainedScore = 0.0;

    @Column(name = "objective_score", nullable = false)
    private Double objectiveScore = 0.0;

    @Column(name = "subjective_score", nullable = false)
    private Double subjectiveScore = 0.0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        ONGOING,      // 进行中
        SUBMITTED,    // 已提交
        EXPIRED       // 已过期
    }

    // Constructors
    public ExamSession() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalDateTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(Double obtainedScore) { this.obtainedScore = obtainedScore; }

    public Double getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }

    public Double getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(Double subjectiveScore) { this.subjectiveScore = subjectiveScore; }

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
