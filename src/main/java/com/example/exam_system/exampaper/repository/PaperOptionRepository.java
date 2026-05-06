package com.example.exam_system.exampaper.repository;

import com.example.exam_system.exampaper.entity.PaperOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaperOptionRepository extends JpaRepository<PaperOption, Long> {
    List<PaperOption> findByPaperQuestionId(Long paperQuestionId);

    void deleteByPaperQuestionId(Long paperQuestionId);
}
