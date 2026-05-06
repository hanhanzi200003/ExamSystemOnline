package com.example.exam_system.examscore.dto;

public class ManualGradeDTO {
    private Long questionId;
    private Double scored;
    private String feedback;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Double getScored() { return scored; }
    public void setScored(Double scored) { this.scored = scored; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
