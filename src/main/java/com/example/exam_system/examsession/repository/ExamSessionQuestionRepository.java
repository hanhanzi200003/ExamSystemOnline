package com.example.exam_system.examsession.repository;

import com.example.exam_system.examsession.entity.ExamSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionQuestionRepository extends JpaRepository<ExamSessionQuestion, Long> {

    List<ExamSessionQuestion> findBySessionIdOrderByDisplayOrder(Long sessionId);

    List<ExamSessionQuestion> findBySessionIdOrderByTemplateOrder(Long sessionId);

    @Query("SELECT esq FROM ExamSessionQuestion esq WHERE esq.sessionId = :sessionId AND esq.questionId = :questionId")
    ExamSessionQuestion findBySessionIdAndQuestionId(@Param("sessionId") Long sessionId, @Param("questionId") Long questionId);

    @Modifying
    @Query("DELETE FROM ExamSessionQuestion esq WHERE esq.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
