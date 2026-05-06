package com.example.exam_system.login.controller;

import com.example.exam_system.login.dto.DeleteAccountRequestDTO;
import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.service.AuthService;
import com.example.exam_system.login.service.PasswordService;
import com.example.exam_system.login.service.UserContextService;
import com.example.exam_system.login.service.UserService;
import com.example.exam_system.login.utils.SecurityValidator;
import com.example.exam_system.login.exception.SecurityValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private PasswordService passwordService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        try {
            String userId = userContextService.getCurrentUserId();
            User user = userService.findByUsername(userId);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "用户不存在"
                ));
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("role", user.getRole().name());
            userData.put("studentStaffId", user.getStudentStaffId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", userData
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取用户信息失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserDTO updateDTO, HttpServletRequest request) {
        try {
            String userId = userContextService.getCurrentUserId();
            User user = userService.findByUsername(userId);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "用户不存在"
                ));
            }

            User updatedUser = userService.updateUserByUsername(userId, updateDTO);
            if (updatedUser != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", updatedUser.getUsername());
                userData.put("nickname", updatedUser.getNickname());
                userData.put("role", updatedUser.getRole().name());

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "个人信息更新成功",
                        "data", userData
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "更新失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "更新个人信息失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            String userId = userContextService.getCurrentUserId();
            User user = userService.findByUsername(userId);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "用户不存在"
                ));
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "密码不能为空"
                ));
            }

            if (!passwordService.matchesPassword(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "当前密码错误"
                ));
            }

            user.setPassword(passwordService.encodePassword(newPassword));
            userService.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "密码修改成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "修改密码失败: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestBody DeleteAccountRequestDTO request, HttpServletRequest httpRequest) {
        try {
            if (request == null) {
                throw new SecurityValidationException("请求参数", "删除账户请求不能为空");
            }

            SecurityValidator.ValidationResult usernameResult = SecurityValidator.validateUsername(request.getUsername());
            if (!usernameResult.isValid()) {
                throw new SecurityValidationException("username", request.getUsername(), usernameResult.getErrorMessages());
            }

            SecurityValidator.ValidationResult passwordResult = SecurityValidator.validatePassword(request.getPassword());
            if (!passwordResult.isValid()) {
                throw new SecurityValidationException("password", "******", passwordResult.getErrorMessages());
            }

            boolean deleted = authService.deleteAccount(request.getUsername(), request.getPassword(), httpRequest);

            if (deleted) {
                return ResponseEntity.ok().body(java.util.Map.of(
                        "success", true,
                        "message", "账户删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "用户名或密码错误"
                ));
            }
        } catch (SecurityValidationException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "message", "安全校验失败: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                    "success", false,
                    "message", "删除账户时发生错误: " + e.getMessage()
            ));
        }
    }
}
