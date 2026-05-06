package com.example.exam_system.examscore.repository;

import com.example.exam_system.examscore.entity.ExamScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {
    Optional<ExamScore> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<ExamScore> findByStudentId(String studentId);

    List<ExamScore> findByExamId(Long examId);

    List<ExamScore> findByStudentIdOrderByCreatedAtDesc(String studentId);

    Page<ExamScore> findByStudentIdContaining(String studentId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ExamScore es WHERE es.studentId = :studentId")
    void deleteByStudentId(@Param("studentId") String studentId);
}
