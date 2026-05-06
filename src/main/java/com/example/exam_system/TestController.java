package com.example.exam_system;

import com.example.exam_system.login.entity.TestUser;
import com.example.exam_system.login.repository.TestUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    private final TestUserRepository testUserRepository;

    public TestController(TestUserRepository testUserRepository) {
        this.testUserRepository = testUserRepository;
    }

    @GetMapping("/users")
    public List<TestUser> users() {
        return testUserRepository.findAll();
    }

    @GetMapping("/api/test/pages")
    public Map<String, Object> testPages() {
        return Map.of(
                "success", true,
                "pages", List.of(
                        "/login - 登录页面",
                        "/register - 注册页面",
                        "/dashboard - 仪表盘页面",
                        "/exam/student - 学生考试页面",
                        "/exam/teacher - 教师考试页面",
                        "/exam/create - 创建试卷页面"
                ),
                "message", "页面路由测试成功"
        );
    }
}
