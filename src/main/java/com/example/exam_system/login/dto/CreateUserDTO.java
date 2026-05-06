package com.example.exam_system.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class CreateUserDTO {

    // 邮箱注册字段
    @Email(message = "请输入有效的邮箱地址")
    private String email;

    // 手机号注册字段
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "角色不能为空")
    private String role;

    @NotBlank(message = "注册类型不能为空")
    private String registrationType; // "EMAIL", "PHONE"

    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9]{2,20}$", message = "昵称只能包含中文、字母和数字，长度 2-20")
    private String nickname;

    @Pattern(regexp = "^[A-Z0-9]{8,12}$", message = "学工号格式不正确，应为 8-12 位大写字母和数字")
    private String studentStaffId;

    @Pattern(regexp = "^\\d{6}$", message = "注册码位数错误")
    private String teacherRegisterCode;

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
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

    public String getTeacherRegisterCode() {
        return teacherRegisterCode;
    }

    public void setTeacherRegisterCode(String teacherRegisterCode) {
        this.teacherRegisterCode = teacherRegisterCode;
    }
}
