// src/main/java/com/example/exam_system/examquestionbank/controller/QuestionBankController.java
package com.example.exam_system.examquestionbank.controller;

import com.example.exam_system.examquestionbank.dto.QuestionCreateDTO;
import com.example.exam_system.examquestionbank.dto.QuestionDTO;
import com.example.exam_system.examquestionbank.service.QuestionBankService;
import com.example.exam_system.login.exception.SecurityValidationException;
import com.example.exam_system.login.service.UserContextService;
import com.example.exam_system.login.utils.SecurityValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-bank")
@PreAuthorize("hasRole('TEACHER')")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private UserContextService userContextService;

    /**
     * 创建题目
     */
    @PostMapping("/questions")
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody QuestionCreateDTO createDTO,
            HttpServletRequest request) {

        // 参数安全校验
        validateQuestionCreateDTO(createDTO);

        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层创建题目
        QuestionDTO question = questionBankService.createQuestion(createDTO, teacherId);

        return ResponseEntity.ok(question);
    }

    /**
     * 获取当前教师的所有题目
     */
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDTO>> getMyQuestions(HttpServletRequest request) {
        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层获取题目列表
        List<QuestionDTO> questions = questionBankService.getQuestionsByTeacher(teacherId);

        return ResponseEntity.ok(questions);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/questions/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层获取题目详情
        QuestionDTO question = questionBankService.getQuestionById(id, teacherId);

        return ResponseEntity.ok(question);
    }

    /**
     * 分页查询题目
     */
    @GetMapping("/questions/page")
    public ResponseEntity<Page<QuestionDTO>> getQuestionsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        String teacherId = userContextService.getCurrentUserId();
        Page<QuestionDTO> questions = questionBankService.getQuestionsByPage(teacherId, pageNum, pageSize);
        return ResponseEntity.ok(questions);
    }

    /**
     * 按题型分页查询题目
     */
    @GetMapping("/questions/type/{questionType}/page")
    public ResponseEntity<Page<QuestionDTO>> getQuestionsByTypeAndPage(
            @PathVariable String questionType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        String teacherId = userContextService.getCurrentUserId();
        Page<QuestionDTO> questions = questionBankService.getQuestionsByTypeAndPage(
                teacherId, questionType, pageNum, pageSize);
        return ResponseEntity.ok(questions);
    }

    /**
     * 批量查询题目（安全版本）
     */
    @PostMapping("/questions/batch")
    public ResponseEntity<List<QuestionDTO>> getQuestionsBatch(
            @RequestBody List<Long> questionIds,
            HttpServletRequest request) {

        String teacherId = userContextService.getCurrentUserId();
        // 使用安全的批量查询方法
        List<QuestionDTO> questions = questionBankService.getQuestionsByIds(questionIds, teacherId);
        return ResponseEntity.ok(questions);
    }


    /**
     * 随机抽取题目（按题型）
     */
    @GetMapping("/questions/random/{questionType}")
    public ResponseEntity<List<QuestionDTO>> getRandomQuestionsByType(
            @PathVariable String questionType,
            @RequestParam int count,
            HttpServletRequest request) {

        String teacherId = userContextService.getCurrentUserId();
        List<QuestionDTO> questions = questionBankService.getRandomQuestionsByType(
                teacherId, questionType, count);
        return ResponseEntity.ok(questions);
    }

    /**
     * 随机抽取题目（任意题型）
     */
    @GetMapping("/questions/random")
    public ResponseEntity<List<QuestionDTO>> getRandomQuestions(
            @RequestParam int count,
            HttpServletRequest request) {

        String teacherId = userContextService.getCurrentUserId();
        List<QuestionDTO> questions = questionBankService.getRandomQuestions(teacherId, count);
        return ResponseEntity.ok(questions);
    }

    /**
     * 更新题目
     */
    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionCreateDTO updateDTO,
            HttpServletRequest request) {

        // 参数安全校验
        validateQuestionCreateDTO(updateDTO);

        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层更新题目
        QuestionDTO question = questionBankService.updateQuestion(id, updateDTO, teacherId);

        return ResponseEntity.ok(question);
    }

    /**
     * 删除题目（物理删除）
     */
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层删除题目
        questionBankService.deleteQuestion(id, teacherId);

        return ResponseEntity.ok(Map.of("success", true, "message", "题目删除成功"));
    }

    /**
     * 导入题目（Excel格式）
     */
    @PostMapping("/import-excel")
    public ResponseEntity<Map<String, Object>> importQuestionsFromExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        // 文件校验
        if (file.isEmpty()) {
            throw new SecurityValidationException("file", "上传文件不能为空");
        }

        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层导入Excel题目
        String result = questionBankService.importQuestionsFromExcel(file, teacherId);

        return ResponseEntity.ok(Map.of("success", true, "message", result));
    }

    /**
     * 按题型筛选题目
     */
    @GetMapping("/questions/type/{questionType}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByType(
            @PathVariable String questionType,
            HttpServletRequest request) {

        // 获取当前教师ID
        String teacherId = userContextService.getCurrentUserId();

        // 调用服务层按题型获取题目
        List<QuestionDTO> questions = questionBankService.getQuestionsByType(teacherId, questionType);

        return ResponseEntity.ok(questions);
    }

    /**
     * 题目参数安全校验
     */
    private void validateQuestionCreateDTO(QuestionCreateDTO dto) {
        // 校验题干
        SecurityValidator.ValidationResult contentResult = SecurityValidator.validateInput(dto.getContent());
        if (!contentResult.isValid()) {
            throw new SecurityValidationException("content", dto.getContent(), contentResult.getErrorMessages());
        }

        // 校验答案（空字符串跳过验证）
        if (dto.getCorrectAnswer() != null && !dto.getCorrectAnswer().trim().isEmpty()) {
            SecurityValidator.ValidationResult answerResult = SecurityValidator.validateInput(dto.getCorrectAnswer());
            if (!answerResult.isValid()) {
                throw new SecurityValidationException("correctAnswer", dto.getCorrectAnswer(), answerResult.getErrorMessages());
            }
        }

        // 校验解析（空字符串跳过验证，解析是可选的）
        if (dto.getAnalysis() != null && !dto.getAnalysis().trim().isEmpty()) {
            SecurityValidator.ValidationResult analysisResult = SecurityValidator.validateInput(dto.getAnalysis());
            if (!analysisResult.isValid()) {
                throw new SecurityValidationException("analysis", dto.getAnalysis(), analysisResult.getErrorMessages());
            }
        }

        // 校验选项（仅限选择题）
        if (dto.getOptions() != null) {
            for (QuestionCreateDTO.OptionDTO option : dto.getOptions()) {
                // 校验选项标签
                if (option.getOptionLabel() == null || option.getOptionLabel().trim().isEmpty()) {
                    throw new SecurityValidationException("optionLabel", "选项标签不能为空");
                }

                // 校验选项内容
                SecurityValidator.ValidationResult optionResult = SecurityValidator.validateInput(option.getOptionContent());
                if (!optionResult.isValid()) {
                    throw new SecurityValidationException("optionContent", option.getOptionContent(), optionResult.getErrorMessages());
                }
            }
        }
    }
}
