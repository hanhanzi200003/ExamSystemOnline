package com.example.exam_system.exampaper.controller;

import com.example.exam_system.exampaper.dto.*;
import com.example.exam_system.exampaper.service.PaperService;
import com.example.exam_system.login.service.UserContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paper")
@PreAuthorize("hasRole('TEACHER')")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private UserContextService userContextService;

    /**
     * 手动组卷
     */
    @PostMapping("/create/manual")
    public ResponseEntity<Map<String, Object>> createManualPaper(
            @RequestBody ManualPaperCreateDTO dto) {

        PaperDetailDTO result = paperService.createManualPaper(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "手动组卷成功");
        response.put("data", result);

        return ResponseEntity.ok(response);
    }

    /**
     * 自动组卷
     */
    @PostMapping("/create/auto")
    public ResponseEntity<Map<String, Object>> createAutoPaper(
            @RequestBody AutoPaperCreateDTO dto) {

        PaperDetailDTO result = paperService.createAutoPaper(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "自动组卷成功");
        response.put("data", result);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取试卷详情
     */
    @GetMapping("/{paperId}")
    public ResponseEntity<Map<String, Object>> getPaperDetail(
            @PathVariable Long paperId) {

        String requesterId = userContextService.getCurrentUserId();
        PaperDetailDTO detail = paperService.getPaperDetail(paperId, requesterId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", detail);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取教师的所有试卷列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getMyPapers() {

        String teacherId = userContextService.getCurrentUserId();
        List<PaperListDTO> papers = paperService.getPapersByTeacher(teacherId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", papers);

        return ResponseEntity.ok(response);
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/{paperId}")
    public ResponseEntity<Map<String, Object>> deletePaper(
            @PathVariable Long paperId) {

        String deleterId = userContextService.getCurrentUserId();
        paperService.deletePaper(paperId, deleterId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "试卷删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 修改试卷（通过替换方式：先删除旧试卷，再创建新试卷）
     */
    @PutMapping("/replace/{oldPaperId}")
    public ResponseEntity<Map<String, Object>> replacePaper(
            @PathVariable Long oldPaperId,
            @RequestBody ManualPaperCreateDTO newPaperDTO) {

        String operatorId = userContextService.getCurrentUserId();
        PaperDetailDTO result = paperService.updatePaperByReplacement(oldPaperId, newPaperDTO, operatorId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "试卷修改成功（已替换为新试卷）");
        response.put("data", result);

        return ResponseEntity.ok(response);
    }
}
