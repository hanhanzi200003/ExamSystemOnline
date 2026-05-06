package com.example.exam_system.login.service;

import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.entity.User;

import java.util.List;

public interface UserManagementService {
    List<User> findAllByRole(String role);
    User findById(Long id);
    User findByUsername(String username);
    User createUser(String username, String rawPassword, String role);
    User updateUser(Long id, UpdateUserDTO updateDTO);
    User updateUserByUsername(String currentUsername, UpdateUserDTO updateDTO);
    boolean deleteUser(Long id);
}
