package com.example.exam_system.examsession.controller;

import com.example.exam_system.examsession.dto.*;
import com.example.exam_system.examsession.service.ExamSessionFacadeService;
import com.example.exam_system.login.service.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-session")
public class ExamSessionController {

    @Autowired
    private ExamSessionFacadeService examSessionService;

    @Autowired
    private UserContextService userContextService;

    @PostMapping("/start/{examId}")
    public ResponseEntity<Map<String, Object>> startExam(@PathVariable Long examId) {
        try {
            ExamStartResponseDTO response = examSessionService.startExam(examId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "考试已开始");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/paper")
    public ResponseEntity<Map<String, Object>> getExamPaper(
            @RequestParam String sessionToken) {
        try {
            ExamSessionDTO paper = examSessionService.getExamPaper(sessionToken);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", paper);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/answer/save")
    public ResponseEntity<Map<String, Object>> saveAnswer(
            @RequestBody AnswerSubmitDTO submitDTO,
            @RequestParam String sessionToken) {
        try {
            examSessionService.saveAnswer(sessionToken, submitDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "答案已保存");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "保存失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/answer/batch-save")
    public ResponseEntity<Map<String, Object>> batchSaveAnswers(
            @RequestBody List<AnswerSubmitDTO> answers,
            @RequestParam String sessionToken) {
        try {
            examSessionService.batchSaveAnswers(sessionToken, answers);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "答案已批量保存");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量保存失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitExam(
            @RequestParam String sessionToken) {
        try {
            Map<String, Object> result = examSessionService.submitExam(sessionToken);

            // 异步批改（在事务外执行）
            Long sessionId = (Long) result.get("sessionId");
            if (sessionId != null) {
                new Thread(() -> {
                    examSessionService.asyncGradeExam(sessionId);
                }).start();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "考试已提交");
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreExamState(
            @RequestParam String sessionToken) {
        try {
            ExamSessionDTO examState = examSessionService.restoreExamState(sessionToken);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", examState);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/check-ongoing")
    public ResponseEntity<Map<String, Object>> checkOngoingExam() {
        try {
            String studentId = userContextService.getCurrentUserId();
            Map<String, Object> ongoingExam = examSessionService.checkOngoingExam(studentId);

            Map<String, Object> result = new HashMap<>();
            if (ongoingExam != null) {
                result.put("hasOngoingExam", true);
                result.put("data", ongoingExam);
            } else {
                result.put("hasOngoingExam", false);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasOngoingExam", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}
