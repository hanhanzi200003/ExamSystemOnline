// src/main/java/com/example/exam_system/login/service/UserContextService.java
package com.example.exam_system.login.service;

import com.example.exam_system.login.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户ID（用户名）
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        throw new IllegalStateException("无法获取当前用户ID");
    }

    /**
     * 获取当前用户角色
     */
    public String getCurrentUserRole(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            return jwtUtil.extractRole(token);
        }
        throw new IllegalStateException("无法获取当前用户角色");
    }

    /**
     * 获取当前token
     */
    public String getCurrentToken(HttpServletRequest request) {
        return getTokenFromRequest(request);
    }

    /**
     * 获取完整用户信息
     */
    public UserInfo getCurrentUserInfo(HttpServletRequest request) {
        return UserInfo.builder()
                .userId(getCurrentUserId())
                .role(getCurrentUserRole(request))
                .token(getCurrentToken(request))
                .build();
    }

    /**
     * 从请求中提取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 用户信息DTO
     */
    public static class UserInfo {
        private String userId;
        private String role;
        private String token;

        private UserInfo(Builder builder) {
            this.userId = builder.userId;
            this.role = builder.role;
            this.token = builder.token;
        }

        // Getters
        public String getUserId() { return userId; }
        public String getRole() { return role; }
        public String getToken() { return token; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String userId;
            private String role;
            private String token;

            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public Builder role(String role) {
                this.role = role;
                return this;
            }

            public Builder token(String token) {
                this.token = token;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(this);
            }
        }
    }
}
