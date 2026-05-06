package com.example.exam_system.login.controller;

import com.example.exam_system.login.dto.CreateUserDTO;
import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.dto.UserResponseDTO;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.service.UserManagementService;
import com.example.exam_system.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherUserController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserService userService;

    // 1. 获取所有学生列表
    @GetMapping("/students")
    public ResponseEntity<List<UserResponseDTO>> getAllStudents() {
        List<User> students = userManagementService.findAllByRole("STUDENT");
        List<UserResponseDTO> responseDTOs = students.stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.getNickname(),
                        user.getStudentStaffId(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRegistrationType()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    // 2. 根据 ID 获取单个学生信息
    @GetMapping("/students/{id}")
    public ResponseEntity<UserResponseDTO> getStudentById(@PathVariable Long id) {
        User user = userManagementService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserResponseDTO responseDTO = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getNickname(),
                user.getStudentStaffId(),
                user.getEmail(),
                user.getPhone(),
                user.getRegistrationType()
        );
        return ResponseEntity.ok(responseDTO);
    }

    // 3. 添加新学生 - 修改为支持邮箱或手机号注册
    @PostMapping("/students")
    public ResponseEntity<UserResponseDTO> createStudent(@RequestBody CreateUserDTO createUserDTO) {
        // 验证传入的角色是否为 STUDENT
        if (!"STUDENT".equalsIgnoreCase(createUserDTO.getRole())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 使用 UserService 的 registerUser 方法来创建用户
            User savedUser = userService.registerUser(createUserDTO);
            UserResponseDTO responseDTO = new UserResponseDTO(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getRole().name(),
                    savedUser.getNickname(),
                    savedUser.getStudentStaffId(),
                    savedUser.getEmail(),
                    savedUser.getPhone(),
                    savedUser.getRegistrationType()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 4. 更新学生信息
    @PutMapping("/students/{id}")
    public ResponseEntity<UserResponseDTO> updateStudent(@PathVariable Long id, @RequestBody UpdateUserDTO updateDTO) {
        User updatedUser = userManagementService.updateUser(id, updateDTO);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        UserResponseDTO responseDTO = new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getRole().name(),
                updatedUser.getNickname(),
                updatedUser.getStudentStaffId(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getRegistrationType()
        );
        return ResponseEntity.ok(responseDTO);
    }
}
