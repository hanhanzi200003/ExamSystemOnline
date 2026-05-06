package com.example.exam_system.examgroup.dto;

import java.time.LocalDateTime;

public class GroupDetailDTO {
    private Long id;
    private String groupCode;
    private String groupName;
    private String description;
    private Long memberCount;
    private String teacherNickname;
    private LocalDateTime createdAt;

    public GroupDetailDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getMemberCount() { return memberCount; }
    public void setMemberCount(Long memberCount) { this.memberCount = memberCount; }

    public String getTeacherNickname() { return teacherNickname; }
    public void setTeacherNickname(String teacherNickname) { this.teacherNickname = teacherNickname; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
