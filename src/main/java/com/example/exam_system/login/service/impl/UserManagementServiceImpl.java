package com.example.exam_system.login.service.impl;

import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import com.example.exam_system.login.service.UserService;
import com.example.exam_system.login.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAllByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public User findById(Long id) {
        return userService.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userService.findByUsername(username);
    }

    @Override
    public User createUser(String username, String rawPassword, String role) {
        return userService.registerUser(username, rawPassword, role);
    }

    @Override
    public User updateUser(Long id, UpdateUserDTO updateDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (updateDTO.getNickname() != null) {
                user.setNickname(updateDTO.getNickname());
            }
            if (updateDTO.getStudentStaffId() != null) {
                user.setStudentStaffId(updateDTO.getStudentStaffId());
            }

            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User updateUserByUsername(String currentUsername, UpdateUserDTO updateDTO) {
        Optional<User> optionalUser = userRepository.findByUsername(currentUsername);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (updateDTO.getNickname() != null) {
                user.setNickname(updateDTO.getNickname());
            }
            if (updateDTO.getStudentStaffId() != null) {
                user.setStudentStaffId(updateDTO.getStudentStaffId());
            }

            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
