package com.example.exam_system.examsession.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 中存储的学生作答数据
 */
public class ExamAnswerCache implements Serializable {

    private Long sessionId;
    private String sessionToken;
    private Long examId;
    private String studentId;
    private String status;
    private Map<Long, String> answers;
    private long lastUpdateTime;

    public ExamAnswerCache() {
        this.answers = new HashMap<>();
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<Long, String> getAnswers() { return answers; }
    public void setAnswers(Map<Long, String> answers) { this.answers = answers; }

    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
}
