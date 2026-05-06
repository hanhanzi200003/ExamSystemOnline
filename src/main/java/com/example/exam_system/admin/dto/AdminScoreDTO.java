package com.example.exam_system.admin.dto;

import java.time.LocalDateTime;

public class AdminScoreDTO {
    private Long id;
    private Long sessionId;
    private String studentId;
    private String studentName;
    private String examName;
    private String groupName;
    private String teacherName;
    private Double totalScore;
    private Double objectiveScore;
    private Double subjectiveScore;
    private Double maxScore;
    private Double scorePercentage;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

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

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
