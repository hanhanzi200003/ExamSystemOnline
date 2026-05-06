package com.example.exam_system.examsession.repository;

import com.example.exam_system.examsession.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    Optional<ExamSession> findByExamIdAndStudentId(Long examId, String studentId);

    Optional<ExamSession> findBySessionToken(String sessionToken);

    @Query("SELECT es FROM ExamSession es WHERE es.examId = :examId AND es.studentId = :studentId AND es.status = 'ONGOING'")
    Optional<ExamSession> findOngoingSession(@Param("examId") Long examId, @Param("studentId") String studentId);

    List<ExamSession> findByStudentId(String studentId);

    List<ExamSession> findByExamId(Long examId);

    @Modifying
    @Query("DELETE FROM ExamSession es WHERE es.studentId = :studentId")
    void deleteByStudentId(@Param("studentId") String studentId);

    @Query("SELECT es FROM ExamSession es WHERE es.status = 'ONGOING' AND es.expectedEndTime < :now")
    List<ExamSession> findExpiredOngoingSessions(@Param("now") LocalDateTime now);

    @Query("SELECT es FROM ExamSession es WHERE es.studentId = :studentId AND es.status = 'ONGOING' AND es.expectedEndTime > :now")
    List<ExamSession> findActiveOngoingSessionsByStudentId(@Param("studentId") String studentId, @Param("now") LocalDateTime now);
}
