package com.example.exam_system.admin.dto;

public class AdminUpdateUserDTO {
    private String nickname;
    private String email;
    private String phone;
    private String studentStaffId;
    private String role;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentStaffId() { return studentStaffId; }
    public void setStudentStaffId(String studentStaffId) { this.studentStaffId = studentStaffId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
