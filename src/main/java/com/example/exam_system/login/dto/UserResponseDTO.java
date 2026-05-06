package com.example.exam_system.login.dto;

// 这个 DTO 用于返回用户信息，注意不包含密码等敏感字段
public class UserResponseDTO {

    private Long id;
    private String username;
    private String role;
    private String nickname;
    private String studentStaffId;
    private String email;
    private String phone;
    private String registrationType;

    // Constructors
    public UserResponseDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public UserResponseDTO(Long id, String username, String role, String nickname, String studentStaffId, String email, String phone, String registrationType) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.nickname = nickname;
        this.studentStaffId = studentStaffId;
        this.email = email;
        this.phone = phone;
        this.registrationType = registrationType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStudentStaffId() {
        return studentStaffId;
    }

    public void setStudentStaffId(String studentStaffId) {
        this.studentStaffId = studentStaffId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }
}
