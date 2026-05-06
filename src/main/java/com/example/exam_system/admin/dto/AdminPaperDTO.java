package com.example.exam_system.admin.dto;

import java.time.LocalDateTime;

public class AdminPaperDTO {
    private Long id;
    private String paperName;
    private String teacherId;
    private String teacherName;
    private Integer questionCount;
    private Double totalScore;
    private String paperType;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public String getPaperType() { return paperType; }
    public void setPaperType(String paperType) { this.paperType = paperType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
