package com.example.exam_system.examscore.repository;

import com.example.exam_system.examscore.entity.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    Optional<ExamRecord> findBySessionId(Long sessionId);

    List<ExamRecord> findByStudentId(String studentId);

    List<ExamRecord> findByStudentIdOrderByStartTimeDesc(String studentId);

    List<ExamRecord> findByStudentIdAndExamId(String studentId, Long examId);

    List<ExamRecord> findByStudentIdAndGroupId(String studentId, Long groupId);

    List<ExamRecord> findByTeacherId(String teacherId);

    List<ExamRecord> findByTeacherIdOrderByStartTimeDesc(String teacherId);

    List<ExamRecord> findByGroupIdOrderByStartTimeDesc(Long groupId);

    List<ExamRecord> findByExamId(Long examId);

    @Query("SELECT er FROM ExamRecord er WHERE er.studentId = :studentId AND er.groupId = :groupId ORDER BY er.startTime DESC")
    List<ExamRecord> findByStudentIdAndGroupIdOrdered(@Param("studentId") String studentId, @Param("groupId") Long groupId);

    boolean existsBySessionId(Long sessionId);
}
