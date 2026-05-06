package com.example.exam_system.examsession.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_session_answers")
public class ExamSessionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "student_answer", columnDefinition = "TEXT")
    private String studentAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "scored", nullable = false)
    private Double scored = 0.0;

    @Column(name = "auto_graded", nullable = false)
    private Boolean autoGraded = false;

    @Column(name = "last_saved_at", nullable = false)
    private LocalDateTime lastSavedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public ExamSessionAnswer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Double getScored() { return scored; }
    public void setScored(Double scored) { this.scored = scored; }

    public Boolean getAutoGraded() { return autoGraded; }
    public void setAutoGraded(Boolean autoGraded) { this.autoGraded = autoGraded; }

    public LocalDateTime getLastSavedAt() { return lastSavedAt; }
    public void setLastSavedAt(LocalDateTime lastSavedAt) { this.lastSavedAt = lastSavedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSavedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastSavedAt = LocalDateTime.now();
    }
}
