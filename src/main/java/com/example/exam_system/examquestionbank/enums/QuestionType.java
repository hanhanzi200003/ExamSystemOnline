// src/main/java/com/example/exampaper/enums/QuestionType.java
package com.example.exam_system.examquestionbank.enums;

public enum QuestionType {
    SINGLE_CHOICE("单选题"),
    MULTIPLE_CHOICE("多选题"),
    TRUE_FALSE("判断题"),
    FILL_BLANK("填空题"),
    SHORT_ANSWER("简答题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}