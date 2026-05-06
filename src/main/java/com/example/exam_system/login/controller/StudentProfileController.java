package com.example.exam_system.login.controller;

import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.dto.UserResponseDTO;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getOwnProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userManagementService.findByUsername(currentUsername);
        if (currentUser == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户不存在");
            return ResponseEntity.status(404).body(errorResponse);
        }
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", currentUser.getId());
        userData.put("username", currentUser.getUsername());
        userData.put("role", currentUser.getRole().name());
        userData.put("nickname", currentUser.getNickname());
        userData.put("studentStaffId", currentUser.getStudentStaffId());
        userData.put("registrationType", currentUser.getRegistrationType());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", userData);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateOwnProfile(@RequestBody UpdateUserDTO updateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User updatedUser = userManagementService.updateUserByUsername(currentUsername, updateDTO);
        if (updatedUser == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户不存在");
            return ResponseEntity.status(404).body(errorResponse);
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", updatedUser.getId());
        userData.put("username", updatedUser.getUsername());
        userData.put("role", updatedUser.getRole().name());
        userData.put("nickname", updatedUser.getNickname());
        userData.put("studentStaffId", updatedUser.getStudentStaffId());
        userData.put("registrationType", updatedUser.getRegistrationType());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "个人信息更新成功");
        response.put("data", userData);
        return ResponseEntity.ok(response);
    }
}
