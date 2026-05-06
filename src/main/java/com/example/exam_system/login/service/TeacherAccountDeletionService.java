package com.example.exam_system.login.service;

import com.example.exam_system.examgroup.entity.Group;
import com.example.exam_system.examgroup.entity.GroupMember;
import com.example.exam_system.examgroup.repository.GroupMemberRepository;
import com.example.exam_system.examgroup.repository.GroupRepository;
import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.exampaper.entity.Paper;
import com.example.exam_system.exampaper.repository.PaperAnswerRepository;
import com.example.exam_system.exampaper.repository.PaperOptionRepository;
import com.example.exam_system.exampaper.repository.PaperQuestionRepository;
import com.example.exam_system.exampaper.repository.PaperRepository;
import com.example.exam_system.examquestionbank.entity.Question;
import com.example.exam_system.examquestionbank.repository.AnswerRepository;
import com.example.exam_system.examquestionbank.repository.OptionRepository;
import com.example.exam_system.examquestionbank.repository.QuestionBankRepository;
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
import com.example.exam_system.login.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherAccountDeletionService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherAccountDeletionService.class);

    @Autowired
    private UserRepository userRepository;

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
    private PaperRepository paperRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Autowired
    private PaperOptionRepository paperOptionRepository;

    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @Autowired
    private ExamScoreDetailRepository examScoreDetailRepository;

    @Transactional
    public boolean deleteTeacherAccount(String username, String rawPassword,
            PasswordService passwordService) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", username);
            return false;
        }

        if (user.getRole() != User.UserRole.TEACHER) {
            logger.warn("User is not a teacher: {}", username);
            return false;
        }

        if (!passwordService.matchesPassword(rawPassword, user.getPassword())) {
            logger.warn("Password mismatch for user: {}", username);
            return false;
        }

        String teacherId = username;

        try {
            logger.info("Starting account deletion for teacher: {}", teacherId);

            deleteExamRecords(teacherId);

            deleteExamsAndSessions(teacherId);

            deleteGroups(teacherId);

            deletePapers(teacherId);

            deleteQuestionBank(teacherId);

            userRepository.delete(user);
            logger.info("Deleted user account: {}", teacherId);

            logger.info("Account deletion completed successfully for teacher: {}", teacherId);
            return true;

        } catch (Exception e) {
            logger.error("Error during account deletion for teacher: {}", teacherId, e);
            throw new RuntimeException("Account deletion failed: " + e.getMessage(), e);
        }
    }

    private void deleteExamRecords(String teacherId) {
        List<ExamRecord> records = examRecordRepository.findByTeacherId(teacherId);
        examRecordRepository.deleteAll(records);
        logger.info("Deleted {} exam records for teacher: {}", records.size(), teacherId);
    }

    private void deleteExamsAndSessions(String teacherId) {
        List<Exam> exams = examRepository.findByCreatorId(teacherId);
        for (Exam exam : exams) {
            List<ExamSession> sessions = examSessionRepository.findByExamId(exam.getId());
            for (ExamSession session : sessions) {
                Long sessionId = session.getId();
                examScoreDetailRepository.deleteBySessionId(sessionId);
                examSessionAnswerRepository.deleteBySessionId(sessionId);
                examSessionQuestionRepository.deleteBySessionId(sessionId);
                examScoreRepository.findBySessionId(sessionId).ifPresent(examScoreRepository::delete);
            }
            examSessionRepository.deleteAll(sessions);
            examRepository.delete(exam);
        }
        logger.info("Deleted {} exams and related sessions for teacher: {}", exams.size(), teacherId);
    }

    private void deleteGroups(String teacherId) {
        List<Group> groups = groupRepository.findByTeacherId(teacherId);
        for (Group group : groups) {
            List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
            groupMemberRepository.deleteAll(members);
            groupRepository.delete(group);
        }
        logger.info("Deleted {} groups for teacher: {}", groups.size(), teacherId);
    }

    private void deletePapers(String teacherId) {
        List<Paper> papers = paperRepository.findByCreatorId(teacherId);
        for (Paper paper : papers) {
            paperQuestionRepository.deleteByPaperId(paper.getId());
            paperRepository.delete(paper);
        }
        logger.info("Deleted {} papers for teacher: {}", papers.size(), teacherId);
    }

    private void deleteQuestionBank(String teacherId) {
        List<Question> questions = questionBankRepository.findByCreatorId(teacherId);
        
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        
        if (!questionIds.isEmpty()) {
            optionRepository.deleteByQuestionIdIn(questionIds);
            answerRepository.deleteByQuestionIdIn(questionIds);
        }
        
        questionBankRepository.deleteAll(questions);
        logger.info("Deleted {} questions and related data for teacher: {}", questions.size(), teacherId);
    }
}
