// src/main/java/com/example/exam_system/config/WebConfig.java
package com.example.exam_system.login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 设置主页重定向到登录页面
        registry.addViewController("/").setViewName("redirect:/log.html");

        // 添加页面路由映射
        registry.addViewController("/login").setViewName("forward:/log.html");
        registry.addViewController("/register").setViewName("forward:/reg.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
        registry.addViewController("/exam/student").setViewName("forward:/exam_student.html");
        registry.addViewController("/exam/teacher").setViewName("forward:/exam_teacher.html");
        registry.addViewController("/exam/create").setViewName("forward:/exam.html");
    }
}
