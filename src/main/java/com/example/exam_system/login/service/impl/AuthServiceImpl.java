package com.example.exam_system.login.service.impl;

import com.example.exam_system.admin.service.TeacherRegisterCodeService;
import com.example.exam_system.login.dto.LoginRequestDTO;
import com.example.exam_system.login.dto.CreateUserDTO;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.service.AuthService;
import com.example.exam_system.login.service.PasswordService;
import com.example.exam_system.login.service.UserService;
import com.example.exam_system.login.utils.SecurityValidator;
import com.example.exam_system.login.service.TokenService;
import com.example.exam_system.login.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TeacherRegisterCodeService teacherRegisterCodeService;

    @Override
    public AuthResult login(LoginRequestDTO request, HttpServletRequest httpRequest) {
        try {
            // 安全校验
            SecurityValidator.ValidationResult usernameResult = SecurityValidator.validateUsername(request.getUsername());
            if (!usernameResult.isValid()) {
                return new AuthResult(false, "用户名格式错误: " + usernameResult.getErrorMessages());
            }

            SecurityValidator.ValidationResult passwordResult = SecurityValidator.validatePassword(request.getPassword());
            if (!passwordResult.isValid()) {
                return new AuthResult(false, "密码格式错误: " + passwordResult.getErrorMessages());
            }

            // 验证凭据
            if (!userService.authenticateUser(request.getUsername(), request.getPassword())) {
                return new AuthResult(false, "用户名或密码错误");
            }

            User user = findUserByAnyField(request.getUsername());
            if (user == null) {
                return new AuthResult(false, "用户不存在");
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            // 检查用户是否已有活跃token，如有则加入黑名单
            String existingToken = tokenService.getUserToken(user.getUsername());
            if (existingToken != null) {
                long remainingTime = tokenService.getRemainingTime(existingToken);
                if (remainingTime > 0) {
                    tokenService.addToBlacklist(existingToken, remainingTime);
                }
            }

            long expirationTime = user.getRole() == User.UserRole.TEACHER ? 2592000000L : 604800000L;
            tokenService.saveUserToken(user.getUsername(), token, expirationTime);

            String authority = "ROLE_" + user.getRole().name();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(authority));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), user.getPassword(), authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return new AuthResult(true, "登录成功", token, user.getRole().name());
        } catch (Exception e) {
            return new AuthResult(false, "登录失败: " + e.getMessage());
        }
    }

    @Override
    public AuthResult register(CreateUserDTO request, HttpServletRequest httpRequest) {
        try {
            if ("TEACHER".equals(request.getRole())) {
                if (request.getTeacherRegisterCode() == null || request.getTeacherRegisterCode().isEmpty()) {
                    return new AuthResult(false, "教师注册需要提供注册码");
                }
                if (!teacherRegisterCodeService.validateCode(request.getTeacherRegisterCode())) {
                    return new AuthResult(false, "教师注册码错误");
                }
            }

            User savedUser = userService.registerUser(request);

            return new AuthResult(true, "注册成功，请登录", null, savedUser.getRole().name());
        } catch (RuntimeException e) {
            return new AuthResult(false, e.getMessage());
        } catch (Exception e) {
            return new AuthResult(false, "注册失败: " + e.getMessage());
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            String username = jwtUtil.extractUsername(token);
            if (username != null) {
                // 将token加入黑名单
                long remainingTime = tokenService.getRemainingTime(token);
                if (remainingTime > 0) {
                    tokenService.addToBlacklist(token, remainingTime);
                }
                // 删除用户的token记录
                tokenService.removeUserToken(username);
            }
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        return userService.authenticateUser(username, password);
    }

    @Override
    public boolean deleteAccount(String username, String password, HttpServletRequest request) {
        boolean deleted = userService.deleteUserAccount(username, password);
        if (deleted) {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                String tokenUsername = jwtUtil.extractUsername(token);
                if (username.equals(tokenUsername)) {
                    // 将token加入黑名单
                    long remainingTime = tokenService.getRemainingTime(token);
                    if (remainingTime > 0) {
                        tokenService.addToBlacklist(token, remainingTime);
                    }
                    // 删除用户的token记录
                    tokenService.removeUserToken(username);
                }
            }
            SecurityContextHolder.clearContext();
        }
        return deleted;
    }

    private User findUserByAnyField(String input) {
        User user = userService.findByUsername(input);
        if (user != null) return user;
        user = userService.findByEmail(input);
        if (user != null) return user;
        user = userService.findByPhone(input);
        return user;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
