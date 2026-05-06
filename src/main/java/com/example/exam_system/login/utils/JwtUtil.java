package com.example.exam_system.login.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // JWT密钥
    private SecretKey secretKey;

    // 学生token有效期7天
    @Value("${jwt.student.expiration:604800000}")
    private Long studentExpiration;

    // 教师token有效期长期有效（设置为30天作为示例）
    @Value("${jwt.teacher.expiration:2592000000}")
    private Long teacherExpiration;

    public JwtUtil(@Value("${jwt.secret:mySecretKeyForExamSystem32Chars12}") String secret) {
        // HS256只需要32字节密钥
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 生成token
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username, role);
    }

    // 创建token - 使用HS256算法
    private String createToken(Map<String, Object> claims, String subject, String role) {
        Long expirationTime = "TEACHER".equals(role.toUpperCase()) ? teacherExpiration : studentExpiration;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)  // 改为HS256
                .compact();
    }

    // 验证token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // 提取用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 提取角色
    public String extractRole(String token) {
        final Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    // 检查token是否过期
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 提取过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 提取声明
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 提取所有声明 - 使用HS256
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
