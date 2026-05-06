// src/main/java/com/example/exam_system/dto/LoginResponseDTO.java
package com.example.exam_system.login.dto;

public class LoginResponseDTO {
    private Boolean success;
    private String message;
    private String token; // 后续可以用 JWT 实现，这里先用字符串模拟
    private String role;  // 返回用户角色

    public LoginResponseDTO(Boolean success, String message, String token, String role) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.role = role;
    }

    // Getter methods
    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}