package com.example.exam_system.examscore.dto;

import java.time.LocalDateTime;

public class ExamRecordDTO {
    private Long id;
    private String studentId;
    private String studentName;
    private String studentStaffId;
    private String teacherId;
    private String teacherName;
    private Long groupId;
    private String groupName;
    private Long examId;
    private String examName;
    private Long sessionId;
    private Double totalScore;
    private Double objectiveScore;
    private Double subjectiveScore;
    private Double maxScore;
    private Double scorePercentage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer actualDurationMinutes;
    private Integer scheduledDurationMinutes;
    private LocalDateTime earliestStartTime;
    private String status;
    private Boolean isSubmitted;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private String remark;
    private LocalDateTime createdAt;

    public ExamRecordDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentStaffId() { return studentStaffId; }
    public void setStudentStaffId(String studentStaffId) { this.studentStaffId = studentStaffId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

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

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }

    public Integer getScheduledDurationMinutes() { return scheduledDurationMinutes; }
    public void setScheduledDurationMinutes(Integer scheduledDurationMinutes) { this.scheduledDurationMinutes = scheduledDurationMinutes; }

    public LocalDateTime getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(LocalDateTime earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsSubmitted() { return isSubmitted; }
    public void setIsSubmitted(Boolean isSubmitted) { this.isSubmitted = isSubmitted; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
