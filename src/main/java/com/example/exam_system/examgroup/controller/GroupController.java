package com.example.exam_system.examgroup.controller;

import com.example.exam_system.examgroup.dto.*;
import com.example.exam_system.examgroup.service.GroupService;
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
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserContextService userContextService;

    @PostMapping("/teacher/create")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GroupDTO> createGroup(@RequestBody GroupCreateDTO createDTO, HttpServletRequest request) {
        String teacherId = userContextService.getCurrentUserId();
        GroupDTO groupDTO = groupService.createGroup(createDTO, teacherId);
        return ResponseEntity.ok(groupDTO);
    }

    @GetMapping("/teacher/my-groups")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<GroupDTO>> getMyGroups(HttpServletRequest request) {
        String teacherId = userContextService.getCurrentUserId();
        List<GroupDTO> groups = groupService.getGroupsByTeacher(teacherId);
        return ResponseEntity.ok(groups);
    }

    @DeleteMapping("/teacher/{groupId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Boolean> deleteGroup(@PathVariable Long groupId, HttpServletRequest request) {
        String teacherId = userContextService.getCurrentUserId();
        boolean result = groupService.deleteGroup(groupId, teacherId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/student/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> joinGroup(@RequestBody JoinGroupDTO joinDTO, HttpServletRequest request) {
        String studentId = userContextService.getCurrentUserId();
        boolean result = groupService.joinGroup(joinDTO.getGroupCode(), studentId);
        
        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "成功加入组");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "加入失败，组码无效或已加入该组");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/student/{groupId}/leave")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> leaveGroup(@PathVariable Long groupId, HttpServletRequest request) {
        String studentId = userContextService.getCurrentUserId();
        boolean result = groupService.leaveGroup(groupId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "已退出组");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "退出失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroupDetail(@PathVariable Long groupId, HttpServletRequest request) {
        String userId = userContextService.getCurrentUserId();
        GroupDTO groupDTO = groupService.getGroupDetail(groupId, userId);
        return ResponseEntity.ok(groupDTO);
    }

    @GetMapping("/student/{groupId}/detail")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GroupDetailDTO> getGroupDetailForStudent(
            @PathVariable Long groupId,
            HttpServletRequest request) {
        String studentId = userContextService.getCurrentUserId();
        GroupDetailDTO groupDetailDTO = groupService.getGroupDetailForStudent(groupId, studentId);
        return ResponseEntity.ok(groupDetailDTO);
    }

    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<GroupMemberDTO>> getGroupMembers(
            @PathVariable Long groupId,
            HttpServletRequest request) {
        String teacherId = userContextService.getCurrentUserId();
        List<GroupMemberDTO> members = groupService.getGroupMembers(groupId, teacherId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/teacher/{groupId}/members/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Boolean> removeMember(
            @PathVariable Long groupId,
            @PathVariable String studentId,
            HttpServletRequest request) {
        String teacherId = userContextService.getCurrentUserId();
        boolean result = groupService.removeMember(groupId, studentId, teacherId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/my-groups")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMyGroupsAsStudent(HttpServletRequest request) {
        String studentId = userContextService.getCurrentUserId();
        List<GroupDTO> groups = groupService.getGroupsByStudent(studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", groups);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/transfer-data")
    public ResponseEntity<TransferDTO> getTransferData(@PathVariable Long groupId) {
        TransferDTO transferDTO = groupService.generateTransferDTO(groupId);
        return ResponseEntity.ok(transferDTO);
    }
}
