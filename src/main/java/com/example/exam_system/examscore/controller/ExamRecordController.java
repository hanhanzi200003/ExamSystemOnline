package com.example.exam_system.examscore.controller;

import com.example.exam_system.examgroup.entity.Group;
import com.example.exam_system.examgroup.repository.GroupRepository;
import com.example.exam_system.examscore.dto.ExamRecordDTO;
import com.example.exam_system.examscore.dto.ExamScoreDTO;
import com.example.exam_system.examscore.entity.ExamRecord;
import com.example.exam_system.examscore.entity.ExamScore;
import com.example.exam_system.examscore.repository.ExamScoreRepository;
import com.example.exam_system.examscore.service.ExamRecordService;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import com.example.exam_system.login.service.UserContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam-record")
public class ExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @GetMapping("/student/my-records")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMyRecords(HttpServletRequest request) {
        String studentId = userContextService.getCurrentUserId();
        List<ExamScore> records = examScoreRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        List<ExamScoreDTO> dtos = records.stream()
                .map(this::convertToExamScoreDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dtos);
        result.put("total", dtos.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/group/{groupId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getRecordsByGroup(
            @PathVariable Long groupId,
            HttpServletRequest request) {
        String studentId = userContextService.getCurrentUserId();
        List<ExamScore> records = examScoreRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        String groupName = groupRepository.findById(groupId)
                .map(Group::getGroupName)
                .orElse("");

        List<ExamScoreDTO> dtos = records.stream()
                .filter(r -> groupName.equals(r.getGroupName()) || r.getGroupName() == null)
                .map(this::convertToExamScoreDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dtos);
        result.put("total", dtos.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{sessionId}/detail")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getRecordDetail(
            @PathVariable Long sessionId) {
        String studentId = userContextService.getCurrentUserId();
        ExamRecord record = examRecordService.getRecordBySessionIdAndStudentId(sessionId, studentId);

        ExamRecordDTO dto = convertToDTO(record);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/teacher/my-students-records")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeacherRecords(HttpServletRequest request) {
        String teacherId = userContextService.getCurrentUserId();
        List<ExamRecord> records = examRecordService.getRecordsByTeacherId(teacherId);

        List<ExamRecordDTO> dtos = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dtos);
        result.put("total", dtos.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/teacher/group/{groupId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getRecordsByGroupForTeacher(
            @PathVariable Long groupId) {
        List<ExamRecord> records = examRecordService.getRecordsByGroupId(groupId);

        List<ExamRecordDTO> dtos = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dtos);
        result.put("total", dtos.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> getRecordById(@PathVariable Long recordId) {
        ExamRecord record = examRecordService.getRecordById(recordId);
        ExamRecordDTO dto = convertToDTO(record);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dto);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/student/{scoreId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> deleteStudentRecord(@PathVariable Long scoreId) {
        String studentId = userContextService.getCurrentUserId();
        
        ExamScore score = examScoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        
        if (!score.getStudentId().equals(studentId)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无权删除此记录");
            return ResponseEntity.status(403).body(result);
        }
        
        examScoreRepository.delete(score);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "记录已删除");
        
        return ResponseEntity.ok(result);
    }

    private ExamScoreDTO convertToExamScoreDTO(ExamScore score) {
        ExamScoreDTO dto = new ExamScoreDTO();
        dto.setId(score.getId());
        dto.setSessionId(score.getSessionId());
        dto.setExamName(score.getExamName());
        dto.setGroupName(score.getGroupName());
        dto.setTeacherName(score.getTeacherName());
        dto.setScheduledDurationMinutes(score.getScheduledDurationMinutes());
        dto.setEarliestStartTime(score.getEarliestStartTime());
        dto.setTotalScore(score.getTotalScore());
        dto.setObjectiveScore(score.getObjectiveScore());
        dto.setSubjectiveScore(score.getSubjectiveScore());
        dto.setMaxScore(score.getMaxScore());
        dto.setScorePercentage(score.getScorePercentage());
        dto.setExamDurationMinutes(score.getExamDurationMinutes());
        dto.setSubmittedAt(score.getSubmittedAt());
        dto.setGradedAt(score.getGradedAt());
        dto.setCreatedAt(score.getCreatedAt());
        return dto;
    }

    private ExamRecordDTO convertToDTO(ExamRecord record) {
        ExamRecordDTO dto = new ExamRecordDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudentId());
        dto.setTeacherId(record.getTeacherId());
        dto.setGroupId(record.getGroupId());
        dto.setExamId(record.getExamId());
        dto.setExamName(record.getExamName());
        dto.setSessionId(record.getSessionId());
        dto.setTotalScore(record.getTotalScore());
        dto.setObjectiveScore(record.getObjectiveScore());
        dto.setSubjectiveScore(record.getSubjectiveScore());
        dto.setMaxScore(record.getMaxScore());
        dto.setScorePercentage(record.getScorePercentage());
        dto.setStartTime(record.getStartTime());
        dto.setEndTime(record.getEndTime());
        dto.setActualDurationMinutes(record.getActualDurationMinutes());
        dto.setScheduledDurationMinutes(record.getScheduledDurationMinutes());
        dto.setEarliestStartTime(record.getEarliestStartTime());
        dto.setStatus(record.getStatus().name());
        dto.setIsSubmitted(record.getIsSubmitted());
        dto.setSubmittedAt(record.getSubmittedAt());
        dto.setGradedAt(record.getGradedAt());
        dto.setRemark(record.getRemark());
        dto.setCreatedAt(record.getCreatedAt());

        if (record.getStudentId() != null) {
            userRepository.findByUsername(record.getStudentId())
                    .ifPresent(user -> {
                        dto.setStudentName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                        dto.setStudentStaffId(user.getStudentStaffId());
                    });
        }

        if (record.getGroupName() != null) {
            dto.setGroupName(record.getGroupName());
        } else if (record.getGroupId() != null) {
            groupRepository.findById(record.getGroupId())
                    .ifPresent(group -> dto.setGroupName(group.getGroupName()));
        }

        if (record.getTeacherName() != null) {
            dto.setTeacherName(record.getTeacherName());
        } else if (record.getTeacherId() != null) {
            userRepository.findByUsername(record.getTeacherId())
                    .ifPresent(user -> dto.setTeacherName(
                            user.getNickname() != null ? user.getNickname() : user.getUsername()
                    ));
        }

        return dto;
    }
}
