package com.example.exam_system.examgroup.dto;

import java.util.List;

public class TransferDTO {
    private Long groupId;
    private String teacherId;
    private List<String> studentIds;

    public TransferDTO() {}

    public TransferDTO(Long groupId, String teacherId, List<String> studentIds) {
        this.groupId = groupId;
        this.teacherId = teacherId;
        this.studentIds = studentIds;
    }

    // Getters and Setters
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public List<String> getStudentIds() { return studentIds; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }
}
