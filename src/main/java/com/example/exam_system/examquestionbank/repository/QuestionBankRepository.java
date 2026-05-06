package com.example.exam_system.examquestionbank.repository;

import com.example.exam_system.examquestionbank.entity.Question;
import com.example.exam_system.examquestionbank.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankRepository extends JpaRepository<Question, Long> {
    List<Question> findByCreatorId(String creatorId);
    List<Question> findByCreatorIdAndQuestionType(String creatorId, QuestionType questionType);

    Page<Question> findByCreatorId(String creatorId, Pageable pageable);
    Page<Question> findByCreatorIdAndQuestionType(String creatorId, QuestionType questionType, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.creatorId = :creatorId AND q.id IN :ids")
    List<Question> findByIdInAndCreatorId(@Param("ids") List<Long> ids, @Param("creatorId") String creatorId);

    List<Question> findByIdIn(List<Long> ids);

    @Query(value = "SELECT * FROM question_bank_questions q WHERE q.creator_id = :creatorId " +
            "AND q.question_type = :questionType " +
            "ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestionsByType(
            @Param("creatorId") String creatorId,
            @Param("questionType") String questionType);

    @Query(value = "SELECT * FROM question_bank_questions q WHERE q.creator_id = :creatorId " +
            "ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestions(
            @Param("creatorId") String creatorId);

    @Modifying
    @Query("DELETE FROM Question q WHERE q.creatorId = :creatorId")
    void deleteByCreatorId(@Param("creatorId") String creatorId);

    Page<Question> findByContentContaining(String content, Pageable pageable);
}
