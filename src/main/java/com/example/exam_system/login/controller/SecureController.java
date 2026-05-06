package com.example.exam_system.login.controller;

import com.example.exam_system.login.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/secure")
public class SecureController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("authenticated", auth.isAuthenticated());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if (username != null && role != null) {
                boolean isValid = jwtUtil.validateToken(token, username);
                response.put("valid", isValid);
                response.put("username", username);
                response.put("role", role);
            } else {
                response.put("valid", false);
                response.put("error", "无法解析token");
            }
        } else {
            response.put("valid", false);
            response.put("error", "缺少有效的Authorization头");
        }

        return ResponseEntity.ok(response);
    }
}
