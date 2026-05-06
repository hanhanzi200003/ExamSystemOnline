package com.example.exam_system.login.exception;

/**
 * 安全校验异常类
 */
public class SecurityValidationException extends RuntimeException {

    private final String field;
    private final String inputValue;

    public SecurityValidationException(String message) {
        super(message);
        this.field = "unknown";
        this.inputValue = "";
    }

    public SecurityValidationException(String field, String message) {
        super(message);
        this.field = field;
        this.inputValue = "";
    }

    public SecurityValidationException(String field, String inputValue, String message) {
        super(message);
        this.field = field;
        this.inputValue = inputValue;
    }

    public String getField() {
        return field;
    }

    public String getInputValue() {
        return inputValue;
    }
}
