// src/main/java/com/example/exam_system/dto/DeleteAccountRequestDTO.java
package com.example.exam_system.login.dto;

public class DeleteAccountRequestDTO {
    private String username;
    private String password;

    // Getters and Setters
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
