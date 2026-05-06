package com.example.exam_system.admin.controller;

import com.example.exam_system.admin.dto.*;
import com.example.exam_system.admin.service.AdminService;
import com.example.exam_system.admin.service.TeacherRegisterCodeService;
import com.example.exam_system.login.service.UserContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private TeacherRegisterCodeService teacherRegisterCodeService;

    // ==================== 教师注册码 ====================

    @GetMapping("/teacher-register-code")
    public ResponseEntity<Map<String, Object>> getTeacherRegisterCode() {
        String code = teacherRegisterCodeService.getRegisterCode();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of("code", code));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/teacher-register-code/refresh")
    public ResponseEntity<Map<String, Object>> refreshTeacherRegisterCode() {
        String code = teacherRegisterCodeService.refreshRegisterCode();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of("code", code));
        result.put("message", "注册码已刷新");
        return ResponseEntity.ok(result);
    }

    // ==================== 统计数据 ====================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getStatistics();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        return ResponseEntity.ok(result);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = adminService.getAllUsers(role, keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        AdminUserDTO user = adminService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", user);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserDTO dto) {
        AdminUserDTO user = adminService.updateUser(id, dto);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", user);
        result.put("message", "用户信息已更新");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long id,
            @RequestBody AdminResetPasswordDTO dto) {
        adminService.resetPassword(id, dto);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码已重置");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "用户已删除");
        return ResponseEntity.ok(result);
    }

    // ==================== 组管理 ====================

    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> getAllGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = adminService.getAllGroups(keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Long id) {
        AdminGroupDTO group = adminService.getGroupById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", group);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable Long id) {
        adminService.deleteGroup(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组已解散");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/groups/{groupId}/members/{studentId}")
    public ResponseEntity<Map<String, Object>> removeGroupMember(
            @PathVariable Long groupId,
            @PathVariable String studentId) {
        adminService.removeGroupMember(groupId, studentId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成员已移除");
        return ResponseEntity.ok(result);
    }

    // ==================== 考试管理 ====================

    @GetMapping("/exams")
    public ResponseEntity<Map<String, Object>> getAllExams(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = adminService.getAllExams(keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exams/{id}")
    public ResponseEntity<Map<String, Object>> getExamById(@PathVariable Long id) {
        AdminExamDTO exam = adminService.getExamById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", exam);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<Map<String, Object>> deleteExam(@PathVariable Long id) {
        adminService.deleteExam(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "考试已删除");
        return ResponseEntity.ok(result);
    }

    // ==================== 试卷管理 ====================

    @GetMapping("/papers")
    public ResponseEntity<Map<String, Object>> getAllPapers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = adminService.getAllPapers(keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/papers/{id}")
    public ResponseEntity<Map<String, Object>> getPaperById(@PathVariable Long id) {
        AdminPaperDTO paper = adminService.getPaperById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", paper);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/papers/{id}")
    public ResponseEntity<Map<String, Object>> deletePaper(@PathVariable Long id) {
        adminService.deletePaper(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "试卷已删除");
        return ResponseEntity.ok(result);
    }

    // ==================== 题目管理 ====================

    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> getAllQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = adminService.getAllQuestions(keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "题目已删除");
        return ResponseEntity.ok(result);
    }

    // ==================== 成绩管理 ====================

    @GetMapping("/scores")
    public ResponseEntity<Map<String, Object>> getAllScores(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = adminService.getAllScores(keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scores/{id}")
    public ResponseEntity<Map<String, Object>> getScoreById(@PathVariable Long id) {
        AdminScoreDTO score = adminService.getScoreById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", score);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/scores/{id}")
    public ResponseEntity<Map<String, Object>> updateScore(
            @PathVariable Long id,
            @RequestBody AdminUpdateScoreDTO dto) {
        AdminScoreDTO score = adminService.updateScore(id, dto);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", score);
        result.put("message", "成绩已修改");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/scores/{id}")
    public ResponseEntity<Map<String, Object>> deleteScore(@PathVariable Long id) {
        adminService.deleteScore(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成绩记录已删除");
        return ResponseEntity.ok(result);
    }
}
