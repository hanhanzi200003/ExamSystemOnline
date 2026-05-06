package com.example.exam_system.examsession.service;

import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examsession.repository.ExamSessionRepository;
import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.login.service.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExamSessionLifecycleService {

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserContextService userContextService;

    /**
     * 创建新的考试会话
     */
    @Transactional
    public ExamSession createSession(Long examId) {
        String studentId = userContextService.getCurrentUserId();

        // 检查是否已有会话
        Optional<ExamSession> existingOpt = examSessionRepository.findByExamIdAndStudentId(examId, studentId);
        if (existingOpt.isPresent()) {
            ExamSession existing = existingOpt.get();
            if (existing.getStatus() == ExamSession.SessionStatus.SUBMITTED) {
                throw new RuntimeException("您已完成该考试，无法再次开始");
            }
            return existing;
        }

        // 获取考试信息并验证
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("考试不存在"));

        validateExamTime(exam);

        // 创建新会话
        LocalDateTime now = LocalDateTime.now();
        ExamSession session = new ExamSession();
        session.setExamId(examId);
        session.setStudentId(studentId);
        session.setSessionToken(generateSessionToken(examId, studentId));
        session.setStatus(ExamSession.SessionStatus.ONGOING);
        session.setStartTime(now);
        session.setExpectedEndTime(now.plusMinutes(exam.getDurationMinutes()));
        session.setPaperId(exam.getPaperId());

        return examSessionRepository.save(session);
    }

    /**
     * 验证会话状态
     */
    public ExamSession validateSession(String sessionToken) {
        ExamSession session = examSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new RuntimeException("无效的考试会话"));

        if (session.getStatus() == ExamSession.SessionStatus.SUBMITTED) {
            throw new RuntimeException("考试已提交");
        }

        if (session.getStatus() == ExamSession.SessionStatus.EXPIRED) {
            throw new RuntimeException("考试已过期");
        }

        if (LocalDateTime.now().isAfter(session.getExpectedEndTime())) {
            session.setStatus(ExamSession.SessionStatus.EXPIRED);
            examSessionRepository.save(session);
            throw new RuntimeException("考试已超时");
        }

        return session;
    }

    /**
     * 更新会话状态
     */
    @Transactional
    public void updateSessionStatus(ExamSession session, ExamSession.SessionStatus status) {
        session.setStatus(status);
        if (status == ExamSession.SessionStatus.SUBMITTED) {
            session.setEndTime(LocalDateTime.now());
        }
        examSessionRepository.save(session);
    }

    private void validateExamTime(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getEarliestStartTime())) {
            throw new RuntimeException("考试尚未开始");
        }
        if (now.isAfter(exam.getLatestStartTime())) {
            throw new RuntimeException("考试已结束");
        }
    }

    private String generateSessionToken(Long examId, String studentId) {
        return UUID.randomUUID().toString().replace("-", "") + "_" + examId + "_" + studentId;
    }
}
