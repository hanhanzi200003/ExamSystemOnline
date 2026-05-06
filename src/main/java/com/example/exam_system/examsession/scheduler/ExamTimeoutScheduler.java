package com.example.exam_system.examsession.scheduler;

import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examsession.repository.ExamSessionRepository;
import com.example.exam_system.examsession.service.ExamSessionFacadeService;
import com.example.exam_system.examscore.service.ExamScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamTimeoutScheduler {

    private final ExamSessionRepository examSessionRepository;
    private final ExamSessionFacadeService examSessionFacadeService;
    private final ExamScoreService examScoreService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndSubmitExpiredExams() {
        log.debug("开始扫描超时考试...");

        try {
            List<ExamSession> expiredSessions = examSessionRepository.findExpiredOngoingSessions(LocalDateTime.now());

            if (expiredSessions.isEmpty()) {
                log.debug("没有超时的考试");
                return;
            }

            log.info("发现 {} 个超时考试，开始自动交卷", expiredSessions.size());

            for (ExamSession session : expiredSessions) {
                try {
                    submitExpiredSession(session);
                } catch (Exception e) {
                    log.error("自动交卷失败，sessionId: {}, 错误: {}", session.getId(), e.getMessage(), e);
                }
            }

            log.info("超时考试自动交卷完成");
        } catch (Exception e) {
            log.error("扫描超时考试失败: {}", e.getMessage(), e);
        }
    }

    private void submitExpiredSession(ExamSession session) {
        log.info("自动交卷 - sessionId: {}, studentId: {}, examId: {}", 
            session.getId(), session.getStudentId(), session.getExamId());

        String sessionToken = session.getSessionToken();

        try {
            examSessionFacadeService.submitExam(sessionToken);
            log.info("自动交卷成功 - sessionId: {}", session.getId());

            try {
                examScoreService.autoGradeObjectiveQuestions(session.getId());
                log.info("自动批改完成 - sessionId: {}", session.getId());
            } catch (Exception e) {
                log.error("自动批改失败 - sessionId: {}, 错误: {}", session.getId(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("自动交卷失败 - sessionId: {}, 错误: {}", session.getId(), e.getMessage());
            
            session.setStatus(ExamSession.SessionStatus.EXPIRED);
            session.setEndTime(LocalDateTime.now());
            examSessionRepository.save(session);
            log.info("考试已标记为过期 - sessionId: {}", session.getId());
        }
    }
}
