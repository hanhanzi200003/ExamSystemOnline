package com.example.exam_system.examscore.controller;

import com.example.exam_system.examscore.dto.GradingQuestionDTO;
import com.example.exam_system.examscore.dto.ManualGradeDTO;
import com.example.exam_system.examscore.dto.ScoreResultDTO;
import com.example.exam_system.examscore.dto.ReviewedPaperDTO;
import com.example.exam_system.examscore.service.ExamScoreService;
import com.example.exam_system.examscore.service.ExcelExportService;
import com.example.exam_system.login.service.UserContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-score")
public class ExamScoreController {

    @Autowired
    private ExamScoreService examScoreService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private UserContextService userContextService;

    @PostMapping("/auto-grade/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> autoGrade(@PathVariable Long sessionId) {
        try {
            ScoreResultDTO result = examScoreService.autoGradeObjectiveQuestions(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "客观题自动批改完成");
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/manual-grade/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> manualGrade(
            @PathVariable Long sessionId,
            @RequestBody List<ManualGradeDTO> grades) {
        try {
            examScoreService.manualGradeSubjectiveQuestions(sessionId, grades);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "主观题批改完成");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/grade-single/{sessionId}/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> gradeSingleQuestion(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Double score = null;
            Object scoreObj = requestBody.get("score");
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).doubleValue();
            }

            if (score == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "分数不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            ManualGradeDTO gradeDTO = new ManualGradeDTO();
            gradeDTO.setQuestionId(questionId);
            gradeDTO.setScored(score);

            examScoreService.manualGradeSubjectiveQuestions(sessionId, List.of(gradeDTO));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批改成功");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getScoreDetail(@PathVariable Long sessionId) {
        try {
            ScoreResultDTO result = examScoreService.getScoreDetail(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/student/{sessionId}/review")
    public ResponseEntity<Map<String, Object>> getReviewedPaper(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        try {
            String studentId = userContextService.getCurrentUserId();
            ReviewedPaperDTO reviewedPaper = examScoreService.getReviewedPaperForStudent(sessionId, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reviewedPaper);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/grading-questions/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getGradingQuestions(@PathVariable Long sessionId) {
        try {
            List<GradingQuestionDTO> questions = examScoreService.getGradingQuestions(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", questions);
            response.put("total", questions.size());

            long gradedCount = questions.stream().filter(GradingQuestionDTO::getIsGraded).count();
            response.put("gradedCount", gradedCount);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/grading-question/{sessionId}/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getSingleGradingQuestion(
            @PathVariable Long sessionId,
            @PathVariable Long questionId) {
        try {
            GradingQuestionDTO question = examScoreService.getSingleGradingQuestion(sessionId, questionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", question);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/pending-grading/{examId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getPendingGradingStudents(@PathVariable Long examId) {
        try {
            List<Map<String, Object>> students = examScoreService.getPendingGradingStudents(examId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", students);
            response.put("total", students.size());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/grading-stats/{examId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getGradingStats(@PathVariable Long examId) {
        try {
            Map<String, Object> stats = examScoreService.getGradingStats(examId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/has-subjective/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> hasSubjectiveQuestions(@PathVariable Long sessionId) {
        try {
            boolean hasSubjective = examScoreService.hasSubjectiveQuestions(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("hasSubjective", hasSubjective));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/export/{examId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportExamScores(@PathVariable Long examId) {
        try {
            byte[] excelData = excelExportService.exportExamScores(examId);
            String fileName = excelExportService.getExportFileName(examId);

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.set("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/{sessionId}/review")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getStudentPaperForTeacher(@PathVariable Long sessionId) {
        try {
            ReviewedPaperDTO reviewedPaper = examScoreService.getReviewedPaperForTeacher(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reviewedPaper);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
