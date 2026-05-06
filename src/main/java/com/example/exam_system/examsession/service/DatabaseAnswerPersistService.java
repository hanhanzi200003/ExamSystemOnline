package com.example.exam_system.examsession.service;

import com.example.exam_system.examsession.dto.AnswerSubmitDTO;
import com.example.exam_system.examsession.entity.ExamSessionAnswer;
import com.example.exam_system.examsession.repository.ExamSessionAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DatabaseAnswerPersistService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseAnswerPersistService.class);

    @Autowired
    private ExamSessionAnswerRepository answerRepository;

    /**
     * 从缓存持久化答案到数据库
     */
    @Transactional
    public void persistFromCache(Long sessionId, Map<Long, String> answers) {
        if (answers == null || answers.isEmpty()) {
            log.warn("没有需要持久化的答案，sessionId: {}", sessionId);
            return;
        }

        log.info("=== 开始持久化答案 === sessionId: {}, 答案数: {}", sessionId, answers.size());

        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String studentAnswer = entry.getValue();
            log.info("持久化答案 - questionId: {} (类型: {}), 学生答案: '{}'", 
                    questionId, questionId != null ? questionId.getClass().getName() : "null", studentAnswer);
            saveOrUpdateAnswer(sessionId, questionId, studentAnswer, now);
        }

        log.info("持久化答案到数据库，sessionId: {}, 题目数：{}", sessionId, answers.size());
    }

    /**
     * 保存单题答案（直接写数据库，用于特殊情况）
     */
    @Transactional
    public void saveAnswerDirectly(Long sessionId, AnswerSubmitDTO submitDTO) {
        saveOrUpdateAnswer(sessionId, submitDTO.getQuestionId(), submitDTO.getStudentAnswer(), LocalDateTime.now());
    }

    private void saveOrUpdateAnswer(Long sessionId, Long questionId, String studentAnswer, LocalDateTime now) {
        Optional<ExamSessionAnswer> answerOpt = answerRepository
                .findBySessionIdAndQuestionId(sessionId, questionId);

        if (answerOpt.isPresent()) {
            ExamSessionAnswer answer = answerOpt.get();
            answer.setStudentAnswer(studentAnswer);
            answer.setLastSavedAt(now);
            answerRepository.save(answer);
        } else {
            ExamSessionAnswer answer = new ExamSessionAnswer();
            answer.setSessionId(sessionId);
            answer.setQuestionId(questionId);
            answer.setStudentAnswer(studentAnswer);
            answer.setAutoGraded(false);
            answer.setScored(0.0);
            answer.setLastSavedAt(now);
            answerRepository.save(answer);
        }
    }
}
