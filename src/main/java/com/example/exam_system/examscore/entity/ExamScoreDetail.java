package com.example.exam_system.examscore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_score_details")
public class ExamScoreDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score_id", nullable = false)
    private Long scoreId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType;

    @Column(name = "question_score", nullable = false)
    private Double questionScore;

    @Column(name = "student_answer", columnDefinition = "TEXT")
    private String studentAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "is_partial_correct")
    private Boolean isPartialCorrect = false;

    @Column(name = "obtained_score")
    private Double obtainedScore;

    @Column(name = "auto_graded", nullable = false)
    private Boolean autoGraded = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ExamScoreDetail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScoreId() { return scoreId; }
    public void setScoreId(Long scoreId) { this.scoreId = scoreId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Double getQuestionScore() { return questionScore; }
    public void setQuestionScore(Double questionScore) { this.questionScore = questionScore; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Boolean getIsPartialCorrect() { return isPartialCorrect; }
    public void setIsPartialCorrect(Boolean isPartialCorrect) { this.isPartialCorrect = isPartialCorrect; }

    public Double getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(Double obtainedScore) { this.obtainedScore = obtainedScore; }

    public Boolean getAutoGraded() { return autoGraded; }
    public void setAutoGraded(Boolean autoGraded) { this.autoGraded = autoGraded; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
