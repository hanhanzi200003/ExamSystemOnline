package com.example.exam_system.examsession.dto;

import java.time.LocalDateTime;

public class ExamResponseDTO {
    private Long sessionId;
    private Long examId;
    private String studentId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;
    private Double totalScore;
    private Double obtainedScore;

    // Getters and Setters
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalDateTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(Double obtainedScore) { this.obtainedScore = obtainedScore; }
}
