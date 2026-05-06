package com.example.exam_system.exammanage.controller;

import com.example.exam_system.exammanage.dto.*;
import com.example.exam_system.exammanage.service.ExamService;
import com.example.exam_system.login.service.UserContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private UserContextService userContextService;

    /**
     * 创建考试（关联试卷）
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createExam(
            @RequestBody ExamCreateManualDTO dto,
            HttpServletRequest request) {

        String creatorId = userContextService.getCurrentUserId();
        ExamResponseDTO responseDTO = examService.createExam(dto, creatorId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "考试创建成功");
        result.put("data", responseDTO);

        return ResponseEntity.ok(result);
    }

    /**
     * 根据 ID 获取考试详细信息
     */
    @GetMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> getExamDetail(@PathVariable Long examId) {
        String requesterId = userContextService.getCurrentUserId();
        ExamDetailDTO detailDTO = examService.getExamDetail(examId, requesterId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", detailDTO);

        return ResponseEntity.ok(result);
    }

    /**
     * 更新考试信息
     */
    @PutMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> updateExam(
            @PathVariable Long examId,
            @RequestBody ExamUpdateDTO dto) {

        String updaterId = userContextService.getCurrentUserId();
        ExamDetailDTO detailDTO = examService.updateExam(examId, dto, updaterId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "考试更新成功");
        result.put("data", detailDTO);

        return ResponseEntity.ok(result);
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> deleteExam(@PathVariable Long examId) {
        String deleterId = userContextService.getCurrentUserId();
        boolean deleted = examService.deleteExam(examId, deleterId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "考试删除成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 获取组内所有考试列表
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getExamsByGroup(@PathVariable Long groupId) {
        String requesterId = userContextService.getCurrentUserId();
        List<ExamListDTO> exams = examService.getExamsByGroup(groupId, requesterId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", exams);

        return ResponseEntity.ok(result);
    }
}
