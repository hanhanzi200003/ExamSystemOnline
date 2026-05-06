package com.example.exam_system.examgroup.service;

import com.example.exam_system.examgroup.dto.*;
import com.example.exam_system.examgroup.entity.Group;
import com.example.exam_system.examgroup.entity.GroupMember;
import com.example.exam_system.examgroup.mapper.GroupMapper;
import com.example.exam_system.examgroup.repository.GroupMemberRepository;
import com.example.exam_system.examgroup.repository.GroupRepository;
import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.examscore.entity.ExamRecord;
import com.example.exam_system.examscore.entity.ExamScore;
import com.example.exam_system.examscore.entity.ExamScoreDetail;
import com.example.exam_system.examscore.repository.ExamRecordRepository;
import com.example.exam_system.examscore.repository.ExamScoreDetailRepository;
import com.example.exam_system.examscore.repository.ExamScoreRepository;
import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examsession.entity.ExamSessionAnswer;
import com.example.exam_system.examsession.entity.ExamSessionQuestion;
import com.example.exam_system.examsession.repository.ExamSessionAnswerRepository;
import com.example.exam_system.examsession.repository.ExamSessionQuestionRepository;
import com.example.exam_system.examsession.repository.ExamSessionRepository;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamSessionAnswerRepository examSessionAnswerRepository;

    @Autowired
    private ExamSessionQuestionRepository examSessionQuestionRepository;

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @Autowired
    private ExamScoreDetailRepository examScoreDetailRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserService userService;

    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    // 教师创建组
    public GroupDTO createGroup(GroupCreateDTO createDTO, String teacherId) {
        String groupCode;
        do {
            groupCode = generateGroupCode();
        } while (groupRepository.existsByGroupCode(groupCode));

        Group group = groupMapper.toEntity(createDTO, teacherId, groupCode);
        Group savedGroup = groupRepository.save(group);

        Long memberCount = groupMemberRepository.countActiveMembersByGroupId(savedGroup.getId());
        return groupMapper.toDTO(savedGroup, memberCount);
    }

    // 教师获取自己的所有组
    public List<GroupDTO> getGroupsByTeacher(String teacherId) {
        List<Group> groups = groupRepository.findByTeacherId(teacherId);
        return groups.stream()
                .map(group -> {
                    Long memberCount = groupMemberRepository.countActiveMembersByGroupId(group.getId());
                    return groupMapper.toDTO(group, memberCount);
                })
                .collect(Collectors.toList());
    }

    // 教师删除组
    public boolean deleteGroup(Long groupId, String teacherId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isPresent() && groupOpt.get().getTeacherId().equals(teacherId)) {
            List<Exam> exams = examRepository.findByGroupId(groupId);
            for (Exam exam : exams) {
                List<ExamSession> sessions = examSessionRepository.findByExamId(exam.getId());
                for (ExamSession session : sessions) {
                    Long sessionId = session.getId();
                    examScoreDetailRepository.deleteBySessionId(sessionId);
                    examSessionAnswerRepository.deleteBySessionId(sessionId);
                    examSessionQuestionRepository.deleteBySessionId(sessionId);
                    examScoreRepository.findBySessionId(sessionId).ifPresent(examScoreRepository::delete);
                    examRecordRepository.findBySessionId(sessionId).ifPresent(examRecordRepository::delete);
                }
                examSessionRepository.deleteAll(sessions);
                examRepository.delete(exam);
            }

            List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
            groupMemberRepository.deleteAll(members);
            groupRepository.deleteById(groupId);
            return true;
        }
        return false;
    }

    // 学生通过组码加入组
    public boolean joinGroup(String groupCode, String studentId) {
        Optional<Group> groupOpt = groupRepository.findByGroupCode(groupCode);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("组不存在");
        }

        Group group = groupOpt.get();

        if (groupMemberRepository.existsByGroupIdAndStudentId(group.getId(), studentId)) {
            throw new RuntimeException("您已在此组中");
        }

        GroupMember member = new GroupMember(group.getId(), studentId);
        groupMemberRepository.save(member);
        return true;
    }

    // 学生退出组
    public boolean leaveGroup(Long groupId, String studentId) {
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupIdAndStudentId(groupId, studentId);
        if (memberOpt.isPresent()) {
            groupMemberRepository.delete(memberOpt.get());
            return true;
        }
        return false;
    }

    // 获取组的详细信息（包括成员列表）
    public GroupDTO getGroupDetail(Long groupId, String requesterId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("组不存在");
        }

        Group group = groupOpt.get();

        boolean isTeacher = group.getTeacherId().equals(requesterId);
        boolean isStudent = groupMemberRepository.existsByGroupIdAndStudentId(groupId, requesterId);

        if (!isTeacher && !isStudent) {
            throw new RuntimeException("无权访问此组");
        }

        Long memberCount = groupMemberRepository.countActiveMembersByGroupId(groupId);
        return groupMapper.toDTO(group, memberCount);
    }

    // 学生查看组详情（显示群组名称、描述、总人数、教师昵称）
    public GroupDetailDTO getGroupDetailForStudent(Long groupId, String studentId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("组不存在");
        }

        Group group = groupOpt.get();

        if (!groupMemberRepository.existsByGroupIdAndStudentId(groupId, studentId)) {
            throw new RuntimeException("您未加入此组");
        }

        User teacher = userService.findByUsername(group.getTeacherId());
        String teacherNickname = teacher != null ? teacher.getNickname() : "未知教师";

        GroupDetailDTO dto = new GroupDetailDTO();
        dto.setId(group.getId());
        dto.setGroupCode(group.getGroupCode());
        dto.setGroupName(group.getGroupName());
        dto.setDescription(group.getDescription());
        dto.setMemberCount(groupMemberRepository.countActiveMembersByGroupId(groupId));
        dto.setTeacherNickname(teacherNickname);
        dto.setCreatedAt(group.getCreatedAt());

        return dto;
    }

    // 获取组的所有成员（仅教师可见）
    public List<GroupMemberDTO> getGroupMembers(Long groupId, String requesterId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("组不存在");
        }

        Group group = groupOpt.get();

        if (!group.getTeacherId().equals(requesterId)) {
            throw new RuntimeException("只有教师可以查看组成员列表");
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(this::convertToMemberDTO)
                .collect(Collectors.toList());
    }

    // 教师移除组成员
    public boolean removeMember(Long groupId, String studentId, String teacherId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent() || !groupOpt.get().getTeacherId().equals(teacherId)) {
            return false;
        }

        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupIdAndStudentId(groupId, studentId);
        if (memberOpt.isPresent()) {
            groupMemberRepository.delete(memberOpt.get());
            return true;
        }
        return false;
    }

    // 生成 TransferDTO 用于模块间传参
    public TransferDTO generateTransferDTO(Long groupId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("组不存在");
        }

        Group group = groupOpt.get();
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        List<String> studentIds = members.stream()
                .map(GroupMember::getStudentId)
                .collect(Collectors.toList());

        return new TransferDTO(groupId, group.getTeacherId(), studentIds);
    }

    // 获取学生加入的所有组
    public List<GroupDTO> getGroupsByStudent(String studentId) {
        List<GroupMember> memberships = groupMemberRepository.findByStudentIdAndStatus(studentId, "ACTIVE");
        List<Long> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toList());

        Map<Long, GroupMember> memberMap = memberships.stream()
                .collect(Collectors.toMap(GroupMember::getGroupId, m -> m));

        List<Group> groups = groupRepository.findAllById(groupIds);
        return groups.stream()
                .map(group -> {
                    Long memberCount = groupMemberRepository.countActiveMembersByGroupId(group.getId());
                    GroupDTO dto = groupMapper.toDTO(group, memberCount);
                    GroupMember member = memberMap.get(group.getId());
                    if (member != null) {
                        dto.setJoinedAt(member.getJoinedAt());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 生成随机 6 位数字组码
    private String generateGroupCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    // 转换 GroupMember 到 GroupMemberDTO
    private GroupMemberDTO convertToMemberDTO(GroupMember member) {
        GroupMemberDTO dto = new GroupMemberDTO();
        dto.setId(member.getId());
        dto.setGroupId(member.getGroupId());
        dto.setStudentId(member.getStudentId());

        User student = userService.findByUsername(member.getStudentId());
        if (student != null) {
            dto.setStudentName(student.getNickname() != null ? student.getNickname() : member.getStudentId());
            dto.setStudentStaffId(student.getStudentStaffId());
        } else {
            dto.setStudentName("学生" + member.getStudentId());
            dto.setStudentStaffId(null);
        }

        dto.setJoinedAt(member.getJoinedAt());
        dto.setStatus(member.getStatus());
        return dto;
    }

    // 验证教师是否属于某个组（供考试管理模块使用）
    public boolean isTeacherMemberOfGroup(Long groupId, String teacherId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            return false;
        }
        return groupOpt.get().getTeacherId().equals(teacherId);
    }
}
