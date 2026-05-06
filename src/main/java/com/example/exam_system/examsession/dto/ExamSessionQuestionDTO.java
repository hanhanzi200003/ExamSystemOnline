package com.example.exam_system.examsession.dto;

import java.util.List;

public class ExamSessionQuestionDTO {
    private Long questionId;
    private Integer templateOrder;
    private Integer displayOrder;
    private String questionType;
    private String questionContent;
    private Double score;
    private List<OptionDTO> options;
    private String studentAnswer;
    private Boolean isCorrect;
    private Double scored;

    public static class OptionDTO {
        private Long id;
        private String optionLabel;
        private String optionContent;
        private Boolean isCorrect;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOptionLabel() { return optionLabel; }
        public void setOptionLabel(String optionLabel) { this.optionLabel = optionLabel; }

        public String getOptionContent() { return optionContent; }
        public void setOptionContent(String optionContent) { this.optionContent = optionContent; }

        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    }

    // Getters and Setters
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Integer getTemplateOrder() { return templateOrder; }
    public void setTemplateOrder(Integer templateOrder) { this.templateOrder = templateOrder; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public List<OptionDTO> getOptions() { return options; }
    public void setOptions(List<OptionDTO> options) { this.options = options; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Double getScored() { return scored; }
    public void setScored(Double scored) { this.scored = scored; }
}
