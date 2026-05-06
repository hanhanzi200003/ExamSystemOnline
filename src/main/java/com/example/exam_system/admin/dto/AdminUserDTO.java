package com.example.exam_system.admin.dto;

import com.example.exam_system.login.entity.User;

public class AdminUserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String studentStaffId;
    private User.UserRole role;
    private String status;
    private String registrationType;
    private java.time.LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentStaffId() { return studentStaffId; }
    public void setStudentStaffId(String studentStaffId) { this.studentStaffId = studentStaffId; }

    public User.UserRole getRole() { return role; }
    public void setRole(User.UserRole role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegistrationType() { return registrationType; }
    public void setRegistrationType(String registrationType) { this.registrationType = registrationType; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
