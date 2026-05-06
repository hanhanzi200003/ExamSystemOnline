package com.example.exam_system.admin.config;

import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import com.example.exam_system.login.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private static final String ADMIN_USERNAME = "zhanghan@qq.com";
    private static final String ADMIN_PASSWORD = "hanhanzi";
    private static final String ADMIN_NICKNAME = "管理员";
    private static final String ADMIN_STAFF_ID = "test";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Override
    public void run(String... args) throws Exception {
        initAdminAccount();
    }

    private void initAdminAccount() {
        if (userRepository.existsByUsername(ADMIN_USERNAME)) {
            logger.info("管理员账户已存在，跳过初始化");
            return;
        }

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordService.encodePassword(ADMIN_PASSWORD));
        admin.setNickname(ADMIN_NICKNAME);
        admin.setEmail(ADMIN_USERNAME);
        admin.setStudentStaffId(ADMIN_STAFF_ID);
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus("ACTIVE");
        admin.setRegistrationType("EMAIL");

        userRepository.save(admin);
        logger.info("========================================");
        logger.info("管理员账户初始化成功！");
        logger.info("用户名: {}", ADMIN_USERNAME);
        logger.info("========================================");
    }
}
