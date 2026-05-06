package com.example.exam_system.exampaper.repository;

import com.example.exam_system.exampaper.entity.PaperQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, Long> {
    List<PaperQuestion> findByPaperIdOrderByQuestionNumber(Long paperId);

    void deleteByPaperId(Long paperId);

    int countByPaperId(Long paperId);
}
