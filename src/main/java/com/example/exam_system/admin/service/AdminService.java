package com.example.exam_system.admin.service;

import com.example.exam_system.admin.dto.*;
import com.example.exam_system.examgroup.entity.Group;
import com.example.exam_system.examgroup.entity.GroupMember;
import com.example.exam_system.examgroup.repository.GroupMemberRepository;
import com.example.exam_system.examgroup.repository.GroupRepository;
import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.exampaper.entity.Paper;
import com.example.exam_system.exampaper.entity.PaperQuestion;
import com.example.exam_system.exampaper.repository.PaperQuestionRepository;
import com.example.exam_system.exampaper.repository.PaperRepository;
import com.example.exam_system.examquestionbank.entity.Question;
import com.example.exam_system.examquestionbank.repository.QuestionBankRepository;
import com.example.exam_system.examscore.entity.ExamScore;
import com.example.exam_system.examscore.repository.ExamScoreRepository;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import com.example.exam_system.login.service.PasswordService;
import com.example.exam_system.login.service.TokenService;
import com.example.exam_system.login.utils.SecurityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private TokenService tokenService;

    // ==================== 用户管理 ====================

    public Map<String, Object> getAllUsers(String role, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (role != null && !role.isEmpty() && keyword != null && !keyword.isEmpty()) {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            userPage = userRepository.findByRoleAndKeyword(userRole, keyword, pageable);
        } else if (role != null && !role.isEmpty()) {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            userPage = userRepository.findByRole(userRole, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByKeyword(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<AdminUserDTO> users = userPage.getContent().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", userPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", userPage.getTotalPages());
        return result;
    }

    public AdminUserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToUserDTO(user);
    }

    @Transactional
    public AdminUserDTO updateUser(Long id, AdminUpdateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getRole() == User.UserRole.ADMIN) {
            throw new RuntimeException("不能修改管理员账号");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getStudentStaffId() != null) {
            user.setStudentStaffId(dto.getStudentStaffId());
        }
        if (dto.getRole() != null) {
            user.setRole(User.UserRole.valueOf(dto.getRole().toUpperCase()));
        }

        userRepository.save(user);
        logger.info("Admin updated user: {}", user.getUsername());
        return convertToUserDTO(user);
    }

    @Transactional
    public void resetPassword(Long id, AdminResetPasswordDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getRole() == User.UserRole.ADMIN) {
            throw new RuntimeException("不能重置管理员密码");
        }

        SecurityValidator.ValidationResult passwordResult = SecurityValidator.validatePassword(dto.getNewPassword());
        if (!passwordResult.isValid()) {
            throw new RuntimeException("密码不符合要求: " + passwordResult.getErrorMessages());
        }

        String encodedPassword = passwordService.encodePassword(dto.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        tokenService.removeUserToken(user.getUsername());
        logger.info("Admin reset password for user: {}", user.getUsername());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getRole() == User.UserRole.ADMIN) {
            throw new RuntimeException("不能删除管理员账号");
        }

        if (user.getRole() == User.UserRole.TEACHER) {
            deleteTeacherData(user.getUsername());
        } else if (user.getRole() == User.UserRole.STUDENT) {
            deleteStudentData(user.getUsername());
        }

        String existingToken = tokenService.getUserToken(user.getUsername());
        if (existingToken != null) {
            long remainingTime = tokenService.getRemainingTime(existingToken);
            if (remainingTime > 0) {
                tokenService.addToBlacklist(existingToken, remainingTime);
            }
        }
        tokenService.removeUserToken(user.getUsername());
        userRepository.delete(user);
        logger.info("Admin deleted user: {}", user.getUsername());
    }

    private void deleteTeacherData(String teacherId) {
        List<Group> groups = groupRepository.findByTeacherId(teacherId);
        for (Group group : groups) {
            groupMemberRepository.deleteByGroupId(group.getId());
            groupRepository.delete(group);
        }

        List<Exam> exams = examRepository.findByCreatorId(teacherId);
        for (Exam exam : exams) {
            examRepository.delete(exam);
        }

        List<Paper> papers = paperRepository.findByCreatorId(teacherId);
        for (Paper paper : papers) {
            paperRepository.delete(paper);
        }

        List<Question> questions = questionBankRepository.findByCreatorId(teacherId);
        for (Question question : questions) {
            questionBankRepository.delete(question);
        }
    }

    private void deleteStudentData(String studentId) {
        examScoreRepository.deleteByStudentId(studentId);
        groupMemberRepository.deleteByStudentId(studentId);
    }

    private AdminUserDTO convertToUserDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStudentStaffId(user.getStudentStaffId());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setRegistrationType(user.getRegistrationType());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // ==================== 组管理 ====================

    public Map<String, Object> getAllGroups(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Group> groupPage;

        if (keyword != null && !keyword.isEmpty()) {
            groupPage = groupRepository.findByGroupNameContaining(keyword, pageable);
        } else {
            groupPage = groupRepository.findAll(pageable);
        }

        List<AdminGroupDTO> groups = groupPage.getContent().stream()
                .map(this::convertToGroupDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("groups", groups);
        result.put("total", groupPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", groupPage.getTotalPages());
        return result;
    }

    public AdminGroupDTO getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("组不存在"));
        return convertToGroupDTO(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("组不存在"));

        groupMemberRepository.deleteByGroupId(id);
        groupRepository.delete(group);
        logger.info("Admin deleted group: {}", group.getGroupName());
    }

    @Transactional
    public void removeGroupMember(Long groupId, String studentId) {
        groupMemberRepository.deleteByGroupIdAndStudentId(groupId, studentId);
        logger.info("Admin removed member {} from group {}", studentId, groupId);
    }

    private AdminGroupDTO convertToGroupDTO(Group group) {
        AdminGroupDTO dto = new AdminGroupDTO();
        dto.setId(group.getId());
        dto.setGroupName(group.getGroupName());
        dto.setTeacherId(group.getTeacherId());
        dto.setCreatedAt(group.getCreatedAt());

        userRepository.findByUsername(group.getTeacherId()).ifPresent(user -> {
            dto.setTeacherName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        });

        int memberCount = groupMemberRepository.countByGroupId(group.getId());
        dto.setMemberCount(memberCount);

        return dto;
    }

    // ==================== 考试管理 ====================

    public Map<String, Object> getAllExams(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Exam> examPage;

        if (keyword != null && !keyword.isEmpty()) {
            examPage = examRepository.findByExamNameContaining(keyword, pageable);
        } else {
            examPage = examRepository.findAll(pageable);
        }

        List<AdminExamDTO> exams = examPage.getContent().stream()
                .map(this::convertToExamDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("exams", exams);
        result.put("total", examPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", examPage.getTotalPages());
        return result;
    }

    public AdminExamDTO getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        return convertToExamDTO(exam);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        examRepository.delete(exam);
        logger.info("Admin deleted exam: {}", exam.getExamName());
    }

    private AdminExamDTO convertToExamDTO(Exam exam) {
        AdminExamDTO dto = new AdminExamDTO();
        dto.setId(exam.getId());
        dto.setExamName(exam.getExamName());
        dto.setTeacherId(exam.getCreatorId());
        dto.setPaperId(exam.getPaperId());
        dto.setGroupId(exam.getGroupId());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setEarliestStartTime(exam.getEarliestStartTime());

        LocalDateTime latestEndTime = null;
        if (exam.getLatestStartTime() != null && exam.getDurationMinutes() != null) {
            latestEndTime = exam.getLatestStartTime().plusMinutes(exam.getDurationMinutes());
        }
        dto.setLatestEndTime(latestEndTime);
        dto.setCreatedAt(exam.getCreatedAt());

        userRepository.findByUsername(exam.getCreatorId()).ifPresent(user -> {
            dto.setTeacherName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        });

        paperRepository.findById(exam.getPaperId()).ifPresent(paper -> {
            dto.setPaperName(paper.getPaperName());
        });

        groupRepository.findById(exam.getGroupId()).ifPresent(group -> {
            dto.setGroupName(group.getGroupName());
        });

        LocalDateTime now = LocalDateTime.now();
        if (latestEndTime != null && now.isAfter(latestEndTime)) {
            dto.setStatus("已结束");
        } else if (exam.getEarliestStartTime() != null && now.isBefore(exam.getEarliestStartTime())) {
            dto.setStatus("未开始");
        } else {
            dto.setStatus("进行中");
        }

        return dto;
    }

    // ==================== 试卷管理 ====================

    public Map<String, Object> getAllPapers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Paper> paperPage;

        if (keyword != null && !keyword.isEmpty()) {
            paperPage = paperRepository.findByPaperNameContaining(keyword, pageable);
        } else {
            paperPage = paperRepository.findAll(pageable);
        }

        List<AdminPaperDTO> papers = paperPage.getContent().stream()
                .map(this::convertToPaperDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("papers", papers);
        result.put("total", paperPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", paperPage.getTotalPages());
        return result;
    }

    public AdminPaperDTO getPaperById(Long id) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        return convertToPaperDTO(paper);
    }

    @Transactional
    public void deletePaper(Long id) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        if (examRepository.existsByPaperId(id)) {
            throw new RuntimeException("该试卷正在被考试使用，无法删除");
        }

        paperRepository.delete(paper);
        logger.info("Admin deleted paper: {}", paper.getPaperName());
    }

    private AdminPaperDTO convertToPaperDTO(Paper paper) {
        AdminPaperDTO dto = new AdminPaperDTO();
        dto.setId(paper.getId());
        dto.setPaperName(paper.getPaperName());
        dto.setTeacherId(paper.getCreatorId());
        dto.setTotalScore(paper.getTotalScore());
        dto.setPaperType(paper.getPaperType() != null ? paper.getPaperType() : "MANUAL");
        dto.setCreatedAt(paper.getCreatedAt());

        userRepository.findByUsername(paper.getCreatorId()).ifPresent(user -> {
            dto.setTeacherName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        });

        int questionCount = paperQuestionRepository.countByPaperId(paper.getId());
        dto.setQuestionCount(questionCount);

        return dto;
    }

    // ==================== 题目管理 ====================

    public Map<String, Object> getAllQuestions(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<Question> questionPage;

        if (keyword != null && !keyword.isEmpty()) {
            questionPage = questionBankRepository.findByContentContaining(keyword, pageable);
        } else {
            questionPage = questionBankRepository.findAll(pageable);
        }

        List<AdminQuestionDTO> questions = questionPage.getContent().stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions);
        result.put("total", questionPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", questionPage.getTotalPages());
        return result;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionBankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        questionBankRepository.delete(question);
        logger.info("Admin deleted question: {}", id);
    }

    private AdminQuestionDTO convertToQuestionDTO(Question question) {
        AdminQuestionDTO dto = new AdminQuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setQuestionType(question.getQuestionType() != null ? question.getQuestionType().name() : "SINGLE_CHOICE");
        dto.setTeacherId(question.getCreatorId());
        dto.setCreatedAt(question.getCreatedTime());

        userRepository.findByUsername(question.getCreatorId()).ifPresent(user -> {
            dto.setTeacherName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        });

        return dto;
    }

    // ==================== 成绩管理 ====================

    public Map<String, Object> getAllScores(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExamScore> scorePage;

        if (keyword != null && !keyword.isEmpty()) {
            scorePage = examScoreRepository.findByStudentIdContaining(keyword, pageable);
        } else {
            scorePage = examScoreRepository.findAll(pageable);
        }

        List<AdminScoreDTO> scores = scorePage.getContent().stream()
                .map(this::convertToScoreDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("scores", scores);
        result.put("total", scorePage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", scorePage.getTotalPages());
        return result;
    }

    public AdminScoreDTO getScoreById(Long id) {
        ExamScore score = examScoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
        return convertToScoreDTO(score);
    }

    @Transactional
    public AdminScoreDTO updateScore(Long id, AdminUpdateScoreDTO dto) {
        ExamScore score = examScoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));

        if (dto.getTotalScore() != null) {
            score.setTotalScore(dto.getTotalScore());
        }
        if (dto.getObjectiveScore() != null) {
            score.setObjectiveScore(dto.getObjectiveScore());
        }
        if (dto.getSubjectiveScore() != null) {
            score.setSubjectiveScore(dto.getSubjectiveScore());
        }

        if (score.getMaxScore() != null && score.getMaxScore() > 0) {
            score.setScorePercentage((score.getTotalScore() / score.getMaxScore()) * 100);
        }

        examScoreRepository.save(score);
        logger.info("Admin updated score: {}", id);
        return convertToScoreDTO(score);
    }

    @Transactional
    public void deleteScore(Long id) {
        ExamScore score = examScoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
        examScoreRepository.delete(score);
        logger.info("Admin deleted score: {}", id);
    }

    private AdminScoreDTO convertToScoreDTO(ExamScore score) {
        AdminScoreDTO dto = new AdminScoreDTO();
        dto.setId(score.getId());
        dto.setSessionId(score.getSessionId());
        dto.setStudentId(score.getStudentId());
        dto.setExamName(score.getExamName());
        dto.setGroupName(score.getGroupName());
        dto.setTeacherName(score.getTeacherName());
        dto.setTotalScore(score.getTotalScore());
        dto.setObjectiveScore(score.getObjectiveScore());
        dto.setSubjectiveScore(score.getSubjectiveScore());
        dto.setMaxScore(score.getMaxScore());
        dto.setScorePercentage(score.getScorePercentage());
        dto.setSubmittedAt(score.getSubmittedAt());
        dto.setGradedAt(score.getGradedAt());

        userRepository.findByUsername(score.getStudentId()).ifPresent(user -> {
            dto.setStudentName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        });

        return dto;
    }

    // ==================== 统计数据 ====================

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalTeachers = userRepository.countByRole(User.UserRole.TEACHER);
        long totalStudents = userRepository.countByRole(User.UserRole.STUDENT);
        long totalGroups = groupRepository.count();
        long totalExams = examRepository.count();
        long totalPapers = paperRepository.count();
        long totalQuestions = questionBankRepository.count();
        long totalScores = examScoreRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalGroups", totalGroups);
        stats.put("totalExams", totalExams);
        stats.put("totalPapers", totalPapers);
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalScores", totalScores);

        return stats;
    }
}
