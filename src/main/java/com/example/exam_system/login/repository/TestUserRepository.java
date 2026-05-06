package com.example.exam_system.login.repository;

import com.example.exam_system.login.entity.TestUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestUserRepository extends JpaRepository<TestUser, Long> {
}
