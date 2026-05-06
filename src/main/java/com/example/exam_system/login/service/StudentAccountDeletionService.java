 package com.example.exam_system.login.service;

import com.example.exam_system.examgroup.repository.GroupMemberRepository;
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
import com.example.exam_system.login.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentAccountDeletionService {

    private static final Logger logger = LoggerFactory.getLogger(StudentAccountDeletionService.class);
    private static final String DELETED_USER_PREFIX = "DELETED_USER_";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

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

    @Transactional
    public boolean deleteStudentAccount(String username, String rawPassword, 
            com.example.exam_system.login.service.PasswordService passwordService) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", username);
            return false;
        }
        
        if (user.getRole() != User.UserRole.STUDENT) {
            logger.warn("User is not a student: {}", username);
            return false;
        }

        if (!passwordService.matchesPassword(rawPassword, user.getPassword())) {
            logger.warn("Password mismatch for user: {}", username);
            return false;
        }

        String studentId = username;
        Long userId = user.getId();
        String anonymousId = DELETED_USER_PREFIX + userId;

        try {
            logger.info("Starting account deletion for student: {}", studentId);

            deleteExamScoreData(studentId);
            
            anonymizeExamRecordData(studentId, anonymousId);
            
            deleteExamSessionData(studentId);

            groupMemberRepository.deleteByStudentId(studentId);
            logger.info("Deleted group memberships for student: {}", studentId);

            userRepository.delete(user);
            logger.info("Deleted user account: {}", studentId);

            logger.info("Account deletion completed successfully for student: {}", studentId);
            return true;

        } catch (Exception e) {
            logger.error("Error during account deletion for student: {}", studentId, e);
            throw new RuntimeException("Account deletion failed: " + e.getMessage(), e);
        }
    }

    private void deleteExamScoreData(String studentId) {
        List<ExamScore> examScores = examScoreRepository.findByStudentId(studentId);
        examScoreRepository.deleteAll(examScores);
        logger.info("Deleted {} exam scores for student: {}", examScores.size(), studentId);
    }

    private void anonymizeExamRecordData(String studentId, String anonymousId) {
        List<ExamRecord> examRecords = examRecordRepository.findByStudentId(studentId);
        for (ExamRecord record : examRecords) {
            record.setStudentId(anonymousId);
            examRecordRepository.save(record);
        }
        logger.info("Anonymized {} exam records for student: {}", examRecords.size(), studentId);
    }

    private void deleteExamSessionData(String studentId) {
        List<ExamSession> sessions = examSessionRepository.findByStudentId(studentId);
        
        for (ExamSession session : sessions) {
            Long sessionId = session.getId();
            
            examScoreDetailRepository.deleteBySessionId(sessionId);
            logger.debug("Deleted score details for session: {}", sessionId);
            
            examSessionAnswerRepository.deleteBySessionId(sessionId);
            logger.debug("Deleted answers for session: {}", sessionId);
            
            examSessionQuestionRepository.deleteBySessionId(sessionId);
            logger.debug("Deleted questions for session: {}", sessionId);
        }

        examSessionRepository.deleteByStudentId(studentId);
        logger.info("Deleted {} exam sessions and related data for student: {}", sessions.size(), studentId);
    }
}
