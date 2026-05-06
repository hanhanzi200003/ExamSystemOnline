package com.example.exam_system.examsession.service;

import com.example.exam_system.examsession.dto.*;
import java.util.List;
import java.util.Map;

public interface IExamSessionService {

    /**
     * 开始考试（首次组卷或恢复考试）
     */
    ExamStartResponseDTO startExam(Long examId);

    /**
     * 获取完整试卷
     */
    ExamSessionDTO getExamPaper(String sessionToken);

    /**
     * 保存单题答案
     */
    void saveAnswer(String sessionToken, AnswerSubmitDTO submitDTO);

    /**
     * 批量保存答案
     */
    void batchSaveAnswers(String sessionToken, List<AnswerSubmitDTO> answers);

    /**
     * 提交试卷
     */
    Map<String, Object> submitExam(String sessionToken);

    /**
     * 恢复考试状态
     */
    ExamSessionDTO restoreExamState(String sessionToken);
}
