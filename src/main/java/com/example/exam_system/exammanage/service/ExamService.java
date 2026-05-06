package com.example.exam_system.exammanage.service;

import com.example.exam_system.examgroup.entity.Group;
import com.example.exam_system.examgroup.repository.GroupMemberRepository;
import com.example.exam_system.examgroup.repository.GroupRepository;
import com.example.exam_system.exampaper.entity.Paper;
import com.example.exam_system.exampaper.repository.PaperRepository;
import com.example.exam_system.exammanage.dto.*;
import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.examscore.entity.ExamRecord;
import com.example.exam_system.examscore.entity.ExamScore;
import com.example.exam_system.examscore.repository.ExamRecordRepository;
import com.example.exam_system.examscore.repository.ExamScoreDetailRepository;
import com.example.exam_system.examscore.repository.ExamScoreRepository;
import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examsession.repository.ExamSessionAnswerRepository;
import com.example.exam_system.examsession.repository.ExamSessionQuestionRepository;
import com.example.exam_system.examsession.repository.ExamSessionRepository;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamSessionQuestionRepository examSessionQuestionRepository;

    @Autowired
    private ExamSessionAnswerRepository examSessionAnswerRepository;

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @Autowired
    private ExamScoreDetailRepository examScoreDetailRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    public ExamResponseDTO createExam(ExamCreateManualDTO dto, String creatorId) {
        if (!isTeacherMemberOfGroup(dto.getGroupId(), creatorId)) {
            throw new RuntimeException("您不是该组的成员，无法创建考试");
        }

        if (!paperRepository.existsByIdAndCreatorId(dto.getPaperId(), creatorId)) {
            throw new RuntimeException("试卷不存在或不属于您");
        }

        validateTime(dto.getEarliestStartTime(), dto.getLatestStartTime());

        Exam exam = new Exam();
        exam.setGroupId(dto.getGroupId());
        exam.setCreatorId(creatorId);
        exam.setExamName(dto.getExamName());
        exam.setDescription(dto.getDescription());
        exam.setPaperId(dto.getPaperId());
        exam.setEarliestStartTime(dto.getEarliestStartTime());
        exam.setLatestStartTime(dto.getLatestStartTime());
        exam.setDurationMinutes(dto.getDurationMinutes());

        Exam savedExam = examRepository.save(exam);

        return convertToResponseDTO(savedExam);
    }

    public ExamDetailDTO getExamDetail(Long examId, String requesterId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (!examOpt.isPresent()) {
            throw new RuntimeException("考试不存在");
        }

        Exam exam = examOpt.get();

        if (!isAuthorizedToAccessGroup(exam.getGroupId(), requesterId)) {
            throw new RuntimeException("无权访问此考试");
        }

        ExamDetailDTO detailDTO = convertToDetailDTO(exam);
        detailDTO.setStatus(calculateExamStatus(exam));

        return detailDTO;
    }

    public ExamDetailDTO updateExam(Long examId, ExamUpdateDTO dto, String updaterId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (!examOpt.isPresent()) {
            throw new RuntimeException("考试不存在");
        }

        Exam exam = examOpt.get();

        if (!exam.getCreatorId().equals(updaterId)) {
            throw new RuntimeException("无权修改此考试");
        }

        if (dto.getEarliestStartTime() != null && dto.getLatestStartTime() != null) {
            validateTime(dto.getEarliestStartTime(), dto.getLatestStartTime());
        }

        if (dto.getPaperId() != null && !dto.getPaperId().equals(exam.getPaperId())) {
            if (!paperRepository.existsByIdAndCreatorId(dto.getPaperId(), updaterId)) {
                throw new RuntimeException("新试卷不存在或不属于您");
            }
            exam.setPaperId(dto.getPaperId());
        }

        if (dto.getExamName() != null) {
            exam.setExamName(dto.getExamName());
        }
        if (dto.getDescription() != null) {
            exam.setDescription(dto.getDescription());
        }
        if (dto.getEarliestStartTime() != null) {
            exam.setEarliestStartTime(dto.getEarliestStartTime());
        }
        if (dto.getLatestStartTime() != null) {
            exam.setLatestStartTime(dto.getLatestStartTime());
        }
        if (dto.getDurationMinutes() != null) {
            exam.setDurationMinutes(dto.getDurationMinutes());
        }


        Exam updatedExam = examRepository.save(exam);

        ExamDetailDTO detailDTO = convertToDetailDTO(updatedExam);
        detailDTO.setStatus(calculateExamStatus(updatedExam));

        return detailDTO;
    }

    public boolean deleteExam(Long examId, String deleterId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (!examOpt.isPresent()) {
            throw new RuntimeException("考试不存在");
        }

        Exam exam = examOpt.get();

        if (!exam.getCreatorId().equals(deleterId)) {
            throw new RuntimeException("无权删除此考试");
        }

        // 级联删除相关数据
        List<ExamSession> sessions = examSessionRepository.findByExamId(examId);
        for (ExamSession session : sessions) {
            Long sessionId = session.getId();
            
            // 删除成绩详情
            examScoreDetailRepository.deleteBySessionId(sessionId);
            
            // 删除学生答案
            examSessionAnswerRepository.deleteBySessionId(sessionId);
            
            // 删除考试题目
            examSessionQuestionRepository.deleteBySessionId(sessionId);
            
            // 删除教师端的考试记录（exam_records）
            examRecordRepository.findBySessionId(sessionId).ifPresent(examRecordRepository::delete);
            
            // 注意：不删除 exam_scores 表的数据，保留学生的考试记录
        }
        
        // 删除考试会话
        examSessionRepository.deleteAll(sessions);
        
        // 删除考试
        examRepository.delete(exam);
        return true;
    }

    public List<ExamListDTO> getExamsByGroup(Long groupId, String requesterId) {
        if (!isAuthorizedToAccessGroup(groupId, requesterId)) {
            throw new RuntimeException("无权访问此组的考试");
        }

        List<Exam> exams = examRepository.findByGroupId(groupId);
        return exams.stream()
                .map(exam -> {
                    ExamListDTO dto = new ExamListDTO();
                    dto.setId(exam.getId());
                    dto.setPaperId(exam.getPaperId());
                    dto.setExamName(exam.getExamName());
                    dto.setDescription(exam.getDescription());
                    dto.setEarliestStartTime(exam.getEarliestStartTime());
                    dto.setLatestStartTime(exam.getLatestStartTime());
                    dto.setDurationMinutes(exam.getDurationMinutes());
                    dto.setStatus(calculateExamStatus(exam));
                    
                    // 查询创建者（教师）名字
                    if (exam.getCreatorId() != null) {
                        User creator = userService.findByUsername(exam.getCreatorId());
                        if (creator != null) {
                            dto.setCreatorName(creator.getNickname() != null ? creator.getNickname() : creator.getUsername());
                        }
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validateTime(LocalDateTime earliestStartTime, LocalDateTime latestStartTime) {
        if (earliestStartTime == null || latestStartTime == null) {
            throw new RuntimeException("最早和最晚进入时间不能为空");
        }

        if (latestStartTime.isBefore(earliestStartTime)) {
            throw new RuntimeException("最晚进入时间不能早于最早可进入时间");
        }
    }

    private String calculateExamStatus(Exam exam) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(exam.getEarliestStartTime())) {
            return "待开始";
        } else if (now.isAfter(exam.getLatestStartTime())) {
            return "已结束";
        } else {
            return "考试中";
        }
    }

    private boolean isAuthorizedToAccessGroup(Long groupId, String userId) {
        if (groupRepository.existsByIdAndTeacherId(groupId, userId)) {
            return true;
        }

        if (groupMemberRepository.existsByGroupIdAndStudentId(groupId, userId)) {
            return true;
        }

        return false;
    }

    private boolean isTeacherMemberOfGroup(Long groupId, String teacherId) {
        return groupRepository.existsByIdAndTeacherId(groupId, teacherId);
    }

    private ExamResponseDTO convertToResponseDTO(Exam exam) {
        ExamResponseDTO dto = new ExamResponseDTO();
        dto.setId(exam.getId());
        dto.setCreatorId(exam.getCreatorId());
        dto.setGroupId(exam.getGroupId());
        dto.setPaperId(exam.getPaperId());
        dto.setExamName(exam.getExamName());
        dto.setEarliestStartTime(exam.getEarliestStartTime());
        dto.setLatestStartTime(exam.getLatestStartTime());
        dto.setDurationMinutes(exam.getDurationMinutes());
        return dto;
    }

    private ExamDetailDTO convertToDetailDTO(Exam exam) {
        ExamDetailDTO dto = new ExamDetailDTO();
        dto.setId(exam.getId());
        dto.setGroupId(exam.getGroupId());
        dto.setCreatorId(exam.getCreatorId());
        dto.setExamName(exam.getExamName());
        dto.setDescription(exam.getDescription());
        dto.setPaperId(exam.getPaperId());
        dto.setEarliestStartTime(exam.getEarliestStartTime());
        dto.setLatestStartTime(exam.getLatestStartTime());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setUpdatedAt(exam.getUpdatedAt());

        User teacher = userService.findByUsername(exam.getCreatorId());
        dto.setTeacherNickname(teacher != null ? teacher.getNickname() : "未知教师");

        return dto;
    }
}
