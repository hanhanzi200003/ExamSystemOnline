package com.example.exam_system.login.service.impl;

import com.example.exam_system.login.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*?";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String generateRandomPassword(int length) {
        if (length < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        StringBuilder password = new StringBuilder(length);
        // 确保包含各种字符类型
        password.append(getRandomChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        password.append(getRandomChar("abcdefghijklmnopqrstuvwxyz"));
        password.append(getRandomChar("0123456789"));
        password.append(getRandomChar("!@#$%^&*?"));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(CHARACTERS));
        }

        // 打乱顺序
        return shuffleString(password.toString());
    }

    private char getRandomChar(String characterSet) {
        return characterSet.charAt(RANDOM.nextInt(characterSet.length()));
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
