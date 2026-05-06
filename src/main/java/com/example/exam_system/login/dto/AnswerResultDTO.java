package com.example.exam_system.login.dto;

public class AnswerResultDTO {
    private boolean correct;

    public AnswerResultDTO(boolean correct) {
        this.correct = correct;
    }

    public boolean isCorrect() {
        return correct;
    }
}
