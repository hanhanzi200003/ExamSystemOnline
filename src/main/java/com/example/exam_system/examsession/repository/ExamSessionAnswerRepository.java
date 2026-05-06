package com.example.exam_system.examsession.repository;

import com.example.exam_system.examsession.entity.ExamSessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionAnswerRepository extends JpaRepository<ExamSessionAnswer, Long> {

    Optional<ExamSessionAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    List<ExamSessionAnswer> findBySessionId(Long sessionId);

    @Query("SELECT esa FROM ExamSessionAnswer esa WHERE esa.sessionId = :sessionId AND esa.questionId IN :questionIds")
    List<ExamSessionAnswer> findBySessionIdAndQuestionIds(@Param("sessionId") Long sessionId, @Param("questionIds") List<Long> questionIds);

    @Modifying
    @Query("DELETE FROM ExamSessionAnswer esa WHERE esa.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
