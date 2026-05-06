package com.example.exam_system.examsession.dto;

public class ExamStartResponseDTO {
    private Long sessionId;
    private String sessionToken;
    private Long examId;
    private String examName;
    private Integer durationMinutes;
    private String startTime;
    private String expectedEndTime;
    private Double totalScore;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(String expectedEndTime) { this.expectedEndTime = expectedEndTime; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
}
