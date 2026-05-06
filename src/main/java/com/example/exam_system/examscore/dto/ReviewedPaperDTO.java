package com.example.exam_system.examscore.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewedPaperDTO {
    private Long sessionId;
    private Long examId;
    private String examName;
    private String studentId;
    private Double totalScore;
    private Double maxScore;
    private Double objectiveScore;
    private Double subjectiveScore;
    private Double scorePercentage;
    private LocalDateTime submittedAt;
    private List<ReviewedQuestionDTO> questions;

    public ReviewedPaperDTO() {}

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Double getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }

    public Double getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(Double subjectiveScore) { this.subjectiveScore = subjectiveScore; }

    public Double getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Double scorePercentage) { this.scorePercentage = scorePercentage; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<ReviewedQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<ReviewedQuestionDTO> questions) { this.questions = questions; }

    // 内部类：题目详情
    public static class ReviewedQuestionDTO {
        private Long questionId;
        private Integer questionNumber; // 题号（按模板卷顺序）
        private String questionType;
        private String questionContent;
        private Double questionScore;
        private String studentAnswer;
        private String correctAnswer;
        private Double obtainedScore;
        private Boolean isCorrect;
        private Boolean isPartialCorrect;
        private String optionsJson;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Integer getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

        public Double getQuestionScore() { return questionScore; }
        public void setQuestionScore(Double questionScore) { this.questionScore = questionScore; }

        public String getStudentAnswer() { return studentAnswer; }
        public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public Double getObtainedScore() { return obtainedScore; }
        public void setObtainedScore(Double obtainedScore) { this.obtainedScore = obtainedScore; }

        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

        public Boolean getIsPartialCorrect() { return isPartialCorrect; }
        public void setIsPartialCorrect(Boolean isPartialCorrect) { this.isPartialCorrect = isPartialCorrect; }

        public String getOptionsJson() { return optionsJson; }
        public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    }
}
