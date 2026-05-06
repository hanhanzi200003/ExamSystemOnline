package com.example.exam_system.examgroup.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "student_id"})
})
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE

    // 构造函数
    public GroupMember() {}

    public GroupMember(Long groupId, String studentId) {
        this.groupId = groupId;
        this.studentId = studentId;
        this.joinedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
