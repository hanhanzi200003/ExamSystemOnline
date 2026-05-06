package com.example.exam_system.login.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.exam_system.login.utils.JwtUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "user:token:";

    // 将token加入黑名单
    public void addToBlacklist(String token, long expirationTime) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    // 检查token是否在黑名单中
    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 保存用户的当前token
    public void saveUserToken(String username, String token, long expirationTime) {
        String key = USER_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, token, expirationTime, TimeUnit.MILLISECONDS);
    }

    // 获取用户的当前token
    public String getUserToken(String username) {
        String key = USER_TOKEN_PREFIX + username;
        return (String) redisTemplate.opsForValue().get(key);
    }

    // 删除用户的token记录
    public void removeUserToken(String username) {
        String key = USER_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
    }

    // 计算token剩余过期时间（从JWT本身解析）
    public Long getRemainingTime(String token) {
        try {
            // 从JWT token中解析过期时间
            Date expiration = jwtUtil.extractExpiration(token);
            if (expiration != null) {
                long expirationTime = expiration.getTime();
                long now = System.currentTimeMillis();
                return Math.max(0, expirationTime - now);
            }
        } catch (Exception e) {
            return -1L;
        }
        return -1L;
    }
}
