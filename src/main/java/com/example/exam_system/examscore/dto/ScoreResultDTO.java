package com.example.exam_system.examscore.dto;

import java.time.LocalDateTime;

public class ScoreResultDTO {
    private Long sessionId;
    private Long examId;
    private String studentId;
    private Double totalScore;
    private Double objectiveScore;
    private Double subjectiveScore;
    private LocalDateTime gradedAt;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }

    public Double getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(Double subjectiveScore) { this.subjectiveScore = subjectiveScore; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
