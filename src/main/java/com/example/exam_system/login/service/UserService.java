package com.example.exam_system.login.service;

import com.example.exam_system.login.dto.CreateUserDTO;
import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.entity.User;

import java.util.List;

public interface UserService {
    // 基础 CRUD 操作
    User save(User user);
    User findById(Long id);
    User findByUsername(String username);
    User findByEmail(String email);
    User findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    void deleteByUsername(String username);
    List<User> findAll();

    // 业务方法
    User registerUser(String username, String rawPassword, String role);
    User registerUser(CreateUserDTO createUserDTO);
    boolean authenticateUser(String username, String rawPassword);
    boolean deleteUserAccount(String username, String rawPassword);

    // 新增更新方法
    User updateUser(Long id, UpdateUserDTO updateDTO);
    User updateUserByUsername(String username, UpdateUserDTO updateDTO);
}
