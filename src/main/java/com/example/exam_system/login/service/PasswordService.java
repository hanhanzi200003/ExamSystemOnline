package com.example.exam_system.login.service;

public interface PasswordService {
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    String encodePassword(String rawPassword);

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean matchesPassword(String rawPassword, String encodedPassword);

    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    String generateRandomPassword(int length);
}
