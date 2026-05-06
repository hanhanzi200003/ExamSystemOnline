package com.example.exam_system.examsession.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ExamSessionDTO {
    private Long sessionId;
    private Long examId;
    private String examName;
    private String sessionToken;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;
    private Integer durationMinutes;
    private Double totalScore;
    private Double obtainedScore;
    private List<ExamSessionQuestionDTO> questions;

    // Getters and Setters
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalDateTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(Double obtainedScore) { this.obtainedScore = obtainedScore; }

    public List<ExamSessionQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<ExamSessionQuestionDTO> questions) { this.questions = questions; }
}
