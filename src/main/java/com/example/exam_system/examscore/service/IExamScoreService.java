package com.example.exam_system.examscore.service;

import com.example.exam_system.examscore.dto.GradingQuestionDTO;
import com.example.exam_system.examscore.dto.ManualGradeDTO;
import com.example.exam_system.examscore.dto.ReviewedPaperDTO;
import com.example.exam_system.examscore.dto.ScoreResultDTO;

import java.util.List;
import java.util.Map;

public interface IExamScoreService {
    
    ScoreResultDTO autoGradeObjectiveQuestions(Long sessionId);

    void manualGradeSubjectiveQuestions(Long sessionId, List<ManualGradeDTO> grades);

    List<GradingQuestionDTO> getGradingQuestions(Long sessionId);

    GradingQuestionDTO getSingleGradingQuestion(Long sessionId, Long questionId);

    ScoreResultDTO getScoreDetail(Long sessionId);

    ReviewedPaperDTO getReviewedPaperForStudent(Long sessionId, String studentId);

    ReviewedPaperDTO getReviewedPaperForTeacher(Long sessionId);

    boolean hasSubjectiveQuestions(Long sessionId);

    boolean isFullyGraded(Long sessionId);

    List<Map<String, Object>> getPendingGradingStudents(Long examId);

    Map<String, Object> getGradingStats(Long examId);
}
