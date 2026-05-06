// src/main/java/com/example/exam_system/dto/LoginRequestDTO.java
package com.example.exam_system.login.dto;

public class LoginRequestDTO {
    private String username;
    private String password;

    // 通常 DTO 也需要 getter/setter，或者使用 Lombok
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}