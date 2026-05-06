package com.example.exam_system.exampaper.repository;

import com.example.exam_system.exampaper.entity.PaperAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaperAnswerRepository extends JpaRepository<PaperAnswer, Long> {
    Optional<PaperAnswer> findByPaperQuestionId(Long paperQuestionId);

    void deleteByPaperQuestionId(Long paperQuestionId);
}
