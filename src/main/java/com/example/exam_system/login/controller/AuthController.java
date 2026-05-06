package com.example.exam_system.login.controller;

import com.example.exam_system.login.dto.LoginRequestDTO;
import com.example.exam_system.login.dto.CreateUserDTO;
import com.example.exam_system.login.dto.LoginResponseDTO;
import com.example.exam_system.login.exception.SecurityValidationException;
import com.example.exam_system.login.service.AuthService;
import com.example.exam_system.login.service.UserService;
import com.example.exam_system.login.utils.SecurityValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    // 提取公共的请求验证方法
    private void validateCommonFields(Object request, String fieldName, String fieldValue) {
        if (request == null) {
            throw new SecurityValidationException("请求参数", fieldName + "请求不能为空");
        }

        SecurityValidator.ValidationResult result = SecurityValidator.validateInput(fieldValue);
        if (!result.isValid()) {
            throw new SecurityValidationException(fieldName, fieldValue, result.getErrorMessages());
        }
    }

    private void validateLoginRequest(LoginRequestDTO request) {
        validateCommonFields(request, "登录", "登录");

        String username = request.getUsername();
        boolean isEmailFormat = username.contains("@") && username.contains(".");

        if (!isEmailFormat) {
            SecurityValidator.ValidationResult phoneResult = SecurityValidator.validatePhone(username);
            if (!phoneResult.isValid()) {
                throw new SecurityValidationException("username", username, phoneResult.getErrorMessages());
            }
        } else {
            if (username.length() > 100) {
                throw new SecurityValidationException("username", username, "邮箱长度超过限制");
            }
            if (!username.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                throw new SecurityValidationException("username", username, "邮箱格式不正确");
            }
        }

        SecurityValidator.ValidationResult passwordResult = SecurityValidator.validatePassword(request.getPassword());
        if (!passwordResult.isValid()) {
            throw new SecurityValidationException("password", "******", passwordResult.getErrorMessages());
        }
    }

    private void validateRegisterRequest(CreateUserDTO request) {
        validateCommonFields(request, "注册", "注册");

        switch (request.getRegistrationType().toUpperCase()) {
            case "EMAIL":
                SecurityValidator.ValidationResult emailResult = SecurityValidator.validateEmail(request.getEmail());
                if (!emailResult.isValid()) {
                    throw new SecurityValidationException("email", request.getEmail(), emailResult.getErrorMessages());
                }
                break;
            case "PHONE":
                SecurityValidator.ValidationResult phoneResult = SecurityValidator.validatePhone(request.getPhone());
                if (!phoneResult.isValid()) {
                    throw new SecurityValidationException("phone", request.getPhone(), phoneResult.getErrorMessages());
                }
                break;
            default:
                throw new SecurityValidationException("registrationType", request.getRegistrationType(), "无效的注册类型");
        }

        SecurityValidator.ValidationResult passwordResult = SecurityValidator.validatePassword(request.getPassword());
        if (!passwordResult.isValid()) {
            throw new SecurityValidationException("password", "******", passwordResult.getErrorMessages());
        }

        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new SecurityValidationException("role", "角色不能为空");
        }

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            SecurityValidator.ValidationResult nicknameResult = SecurityValidator.validateInput(request.getNickname());
            if (!nicknameResult.isValid()) {
                throw new SecurityValidationException("nickname", request.getNickname(), nicknameResult.getErrorMessages());
            }
        }

        if (request.getStudentStaffId() != null && !request.getStudentStaffId().isEmpty()) {
            SecurityValidator.ValidationResult studentStaffIdResult = SecurityValidator.validateInput(request.getStudentStaffId());
            if (!studentStaffIdResult.isValid()) {
                throw new SecurityValidationException("studentStaffId", request.getStudentStaffId(), studentStaffIdResult.getErrorMessages());
            }
        }
    }


    // 添加账户存在性检查端点
    @GetMapping("/check-registration")
    public ResponseEntity<Map<String, Object>> checkRegistration(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (email != null && !email.trim().isEmpty()) {
                boolean exists = userService.existsByEmail(email.trim());
                response.put("emailExists", exists);
                if (exists) {
                    response.put("message", "该邮箱已被注册");
                }
            }

            if (phone != null && !phone.trim().isEmpty()) {
                boolean exists = userService.existsByPhone(phone.trim());
                response.put("phoneExists", exists);
                if (exists) {
                    response.put("message", "该手机号已被注册");
                }
            }

            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查过程中发生错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        try {
            validateLoginRequest(request);
            AuthService.AuthResult result = authService.login(request, httpRequest);

            LoginResponseDTO response = new LoginResponseDTO(
                    result.isSuccess(),
                    result.getMessage(),
                    result.getToken(),
                    result.getRole()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new LoginResponseDTO(false, "登录过程中发生错误", null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody CreateUserDTO request, HttpServletRequest httpRequest) {
        try {
            validateRegisterRequest(request);
            AuthService.AuthResult result = authService.register(request, httpRequest);

            LoginResponseDTO response = new LoginResponseDTO(
                    result.isSuccess(),
                    result.getMessage(),
                    result.getToken(),
                    result.getRole()
            );

            return ResponseEntity.status(result.isSuccess() ? 200 : 400).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new LoginResponseDTO(false, "注册过程中发生错误", null, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDTO> logout(HttpServletRequest request) {
        try {
            authService.logout(request);
            return ResponseEntity.ok(new LoginResponseDTO(true, "登出成功", null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new LoginResponseDTO(false, "登出过程中发生错误: " + e.getMessage(), null, null));
        }
    }



}
