package com.example.exam_system.exampaper.enums;

public enum PaperType {
    MANUAL("手动组卷"),
    AUTO("自动组卷");

    private final String description;

    PaperType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
