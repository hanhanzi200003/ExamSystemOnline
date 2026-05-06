package com.example.exam_system.examscore.dto;

import java.time.LocalDateTime;

public class GradingQuestionDTO {
    private Long questionId;
    private Integer questionNumber;
    private String questionType;
    private Double questionScore;
    private String questionContent;
    private String studentAnswer;
    private Double obtainedScore;
    private Boolean isGraded;
    private LocalDateTime gradedAt;
    private String correctAnswer;
    private String explanation;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Double getQuestionScore() { return questionScore; }
    public void setQuestionScore(Double questionScore) { this.questionScore = questionScore; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public Double getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(Double obtainedScore) { this.obtainedScore = obtainedScore; }

    public Boolean getIsGraded() { return isGraded; }
    public void setIsGraded(Boolean isGraded) { this.isGraded = isGraded; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
