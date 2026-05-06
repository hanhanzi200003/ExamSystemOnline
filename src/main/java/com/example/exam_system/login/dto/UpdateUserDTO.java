package com.example.exam_system.login.dto;

import jakarta.validation.constraints.Pattern;

public class UpdateUserDTO {

    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9]{2,20}$", message = "昵称只能包含中文、字母和数字，长度 2-20")
    private String nickname;

    @Pattern(regexp = "^[A-Z0-9]{8,12}$", message = "学工号格式不正确，应为 8-12 位大写字母和数字")
    private String studentStaffId;

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
}
