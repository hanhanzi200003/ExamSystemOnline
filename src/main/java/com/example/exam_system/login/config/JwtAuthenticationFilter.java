package com.example.exam_system.login.config;

import com.example.exam_system.login.service.SecurityUserDetailsService;
import com.example.exam_system.login.service.TokenService;
import com.example.exam_system.login.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityUserDetailsService userDetailsService;

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authorizationHeader = request.getHeader("Authorization");

        logger.debug("Processing request: {} with Authorization header: {}", requestURI,
                authorizationHeader != null ? "present" : "missing");

        // 跳过不需要认证的路径
        if (shouldNotFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);

        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                logger.debug("Extracted username from token: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 验证token是否在黑名单中
                    if (tokenService.isTokenBlacklisted(token)) {
                        logger.warn("Token is blacklisted: {}", username);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Token已被注销\",\"code\":401}");
                        return;
                    }

                    // 验证JWT token有效性
                    if (jwtUtil.validateToken(token, username)) {
                        logger.debug("Token validated successfully for user: {}", username);

                        // 验证Redis中的token是否匹配
                        String redisToken = tokenService.getUserToken(username);
                        if (redisToken != null && redisToken.equals(token)) {
                            // 从UserDetailsService加载用户详情
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                            // 建立认证上下文
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("Authentication context set for user: {}", username);
                        } else {
                            logger.warn("Redis token mismatch for user: {}", username);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"账户已在其他设备登录\",\"code\":401}");
                            return;
                        }
                    } else {
                        logger.warn("Token validation failed for user: {}", username);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Token无效\",\"code\":401}");
                        return;
                    }
                }
            } catch (Exception e) {
                // Token无效，清除认证上下文
                logger.debug("Invalid JWT token: " + e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Token解析失败\",\"code\":401}");
                return;
            }
        } else {
            logger.debug("No token found in request");
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldNotFilter(String requestURI) {
        return requestURI.startsWith("/api/auth/") ||
                requestURI.startsWith("/index.html") ||
                requestURI.equals("/") ||
                requestURI.startsWith("/log.html") ||
                requestURI.startsWith("/reg.html") ||
                requestURI.startsWith("/dashboard.html") ||
                (requestURI.startsWith("/api/test/") && !requestURI.equals("/api/test/auth-status"));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
