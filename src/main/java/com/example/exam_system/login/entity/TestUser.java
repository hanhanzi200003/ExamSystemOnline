package com.example.exam_system.login.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_user")
public class TestUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Integer age;

    // 必须要有无参构造
    public TestUser() {}

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Integer getAge() {
        return age;
    }
}
