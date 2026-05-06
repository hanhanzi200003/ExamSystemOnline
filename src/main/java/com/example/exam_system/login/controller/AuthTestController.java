package com.example.exam_system.login.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthTestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthTestController.class);

    @GetMapping("/api/test/auth-status")
    public Map<String, Object> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        logger.debug("Auth status check - Auth object: {}", auth);
        logger.debug("Is authenticated: {}", auth != null ? auth.isAuthenticated() : false);
        logger.debug("Principal: {}", auth != null ? auth.getPrincipal() : "null");

        response.put("timestamp", System.currentTimeMillis());
        response.put("sessionId", SecurityContextHolder.getContext().hashCode());

        // 添加调试信息
        response.put("hasAuthorizationHeader",
                SecurityContextHolder.getContext().getAuthentication() != null);

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
            response.put("principal", auth.getPrincipal());
            response.put("details", auth.getDetails());

            // 提取用户角色
            String userRole = "UNKNOWN";
            if (!auth.getAuthorities().isEmpty()) {
                String authority = auth.getAuthorities().iterator().next().getAuthority();
                userRole = authority.replace("ROLE_", "");
            }
            response.put("role", userRole);

            logger.debug("User {} is authenticated with authorities: {}",
                    auth.getName(), auth.getAuthorities());
        } else {
            response.put("authenticated", false);
            response.put("message", "User is not authenticated");
            response.put("authObject", auth != null ? auth.toString() : "null");
            response.put("principalCheck", auth != null ? auth.getPrincipal() : "null auth");
            logger.debug("Authentication failed - auth object: {}", auth);
        }

        return response;
    }

    @GetMapping("/api/test/public-test")
    public Map<String, Object> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "This is a public test endpoint");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
