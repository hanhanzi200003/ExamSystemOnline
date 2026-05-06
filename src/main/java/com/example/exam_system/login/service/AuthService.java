package com.example.exam_system.login.service;

import com.example.exam_system.login.dto.LoginRequestDTO;
import com.example.exam_system.login.dto.CreateUserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResult login(LoginRequestDTO request, HttpServletRequest httpRequest);
    AuthResult register(CreateUserDTO request, HttpServletRequest httpRequest);
    void logout(HttpServletRequest request);
    boolean validateCredentials(String username, String password);
    boolean deleteAccount(String username, String password, HttpServletRequest request);

    class AuthResult {
        private boolean success;
        private String message;
        private String token;
        private String role;

        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public AuthResult(boolean success, String message, String token, String role) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.role = role;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
