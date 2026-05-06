// src/main/java/com/example/exam_system/examquestionbank/repository/AnswerRepository.java
package com.example.exam_system.examquestionbank.repository;

import com.example.exam_system.examquestionbank.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestionId(Long questionId);
    void deleteByQuestionId(Long questionId);
    void deleteByQuestionIdIn(List<Long> questionIds);
}
