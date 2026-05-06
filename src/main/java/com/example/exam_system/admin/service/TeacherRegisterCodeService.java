package com.example.exam_system.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;

@Service
public class TeacherRegisterCodeService {

    private static final Logger log = LoggerFactory.getLogger(TeacherRegisterCodeService.class);
    
    private String registerCode;
    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    public void init() {
        generateNewCode();
        log.info("教师注册码已生成: {}", registerCode);
    }

    public synchronized String getRegisterCode() {
        return registerCode;
    }

    public synchronized String refreshRegisterCode() {
        generateNewCode();
        log.info("教师注册码已刷新: {}", registerCode);
        return registerCode;
    }

    public boolean validateCode(String inputCode) {
        if (inputCode == null || inputCode.length() != 6) {
            return false;
        }
        
        if (!inputCode.matches("\\d{6}")) {
            return false;
        }
        
        return inputCode.equals(registerCode);
    }

    private void generateNewCode() {
        int code = 100000 + random.nextInt(900000);
        registerCode = String.valueOf(code);
    }
}
