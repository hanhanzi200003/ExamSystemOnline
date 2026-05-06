package com.example.exam_system.examsession.dto;

public class AnswerSubmitDTO {
    private Long questionId;
    private String studentAnswer;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
}
