package com.example.exam_system.examscore.service;

import com.example.exam_system.examscore.entity.ExamRecord;
import com.example.exam_system.examscore.repository.ExamRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExamRecordService {

    @Autowired
    private ExamRecordRepository examRecordRepository;

    public List<ExamRecord> getRecordsByStudentId(String studentId) {
        return examRecordRepository.findByStudentIdOrderByStartTimeDesc(studentId);
    }

    public List<ExamRecord> getRecordsByStudentIdAndGroupId(String studentId, Long groupId) {
        return examRecordRepository.findByStudentIdAndGroupIdOrdered(studentId, groupId);
    }

    public ExamRecord getRecordBySessionIdAndStudentId(Long sessionId, String studentId) {
        return examRecordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("考试记录不存在"));
    }

    public List<ExamRecord> getRecordsByTeacherId(String teacherId) {
        return examRecordRepository.findByTeacherIdOrderByStartTimeDesc(teacherId);
    }

    public List<ExamRecord> getRecordsByGroupId(Long groupId) {
        return examRecordRepository.findByGroupIdOrderByStartTimeDesc(groupId);
    }

    public ExamRecord getRecordById(Long recordId) {
        return examRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("考试记录不存在"));
    }

    @Transactional
    public void deleteExamRecord(Long recordId) {
        ExamRecord record = getRecordById(recordId);
        examRecordRepository.delete(record);
    }
}
