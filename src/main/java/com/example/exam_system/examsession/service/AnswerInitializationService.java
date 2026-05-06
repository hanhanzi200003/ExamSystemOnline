package com.example.exam_system.examsession.service;

import com.example.exam_system.examsession.entity.ExamSessionAnswer;
import com.example.exam_system.examsession.repository.ExamSessionAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerInitializationService {

    private static final Logger log = LoggerFactory.getLogger(AnswerInitializationService.class);

    @Autowired
    private ExamSessionAnswerRepository answerRepository;

    /**
     * 预创建所有题目的答案记录
     */
    @Transactional
    public void initializeAnswers(Long sessionId, List<Long> questionIds) {
        List<ExamSessionAnswer> answers = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long questionId : questionIds) {
            ExamSessionAnswer answer = new ExamSessionAnswer();
            answer.setSessionId(sessionId);
            answer.setQuestionId(questionId);
            answer.setStudentAnswer("");
            answer.setAutoGraded(false);
            answer.setScored(0.0);
            answer.setLastSavedAt(now);
            answers.add(answer);
        }

        answerRepository.saveAll(answers);
        log.info("预创建答案记录，sessionId: {}, 题目数：{}", sessionId, questionIds.size());
    }
}
