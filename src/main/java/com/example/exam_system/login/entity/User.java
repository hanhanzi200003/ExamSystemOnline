package com.example.exam_system.login.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "users")
public class User {

    public enum UserRole {
        ADMIN, TEACHER, STUDENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Email(message = "请输入有效的邮箱地址")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    @Column(unique = true)
    private String phone;

    @Column(name = "registration_type", nullable = false)
    private String registrationType;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "student_staff_id", unique = true)
    private String studentStaffId;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    public User() {
        this.status = "ACTIVE";
        this.createdAt = java.time.LocalDateTime.now();
    }

    public User(String email, String password, UserRole role, boolean isEmailRegistration) {
        this.email = email;
        this.username = email;
        this.password = password;
        this.role = role;
        this.registrationType = "EMAIL";
        this.status = "ACTIVE";
        this.createdAt = java.time.LocalDateTime.now();
    }

    public User(String phone, String password, UserRole role) {
        this.phone = phone;
        this.username = phone;
        this.password = password;
        this.role = role;
        this.registrationType = "PHONE";
        this.status = "ACTIVE";
        this.createdAt = java.time.LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRegistrationType() { return registrationType; }
    public void setRegistrationType(String registrationType) { this.registrationType = registrationType; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getStudentStaffId() { return studentStaffId; }
    public void setStudentStaffId(String studentStaffId) { this.studentStaffId = studentStaffId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
