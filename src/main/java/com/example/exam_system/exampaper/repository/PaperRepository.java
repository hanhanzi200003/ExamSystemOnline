package com.example.exam_system.exampaper.repository;

import com.example.exam_system.exampaper.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {
    List<Paper> findByCreatorId(String creatorId);

    boolean existsByIdAndCreatorId(Long id, String creatorId);

    Page<Paper> findByPaperNameContaining(String paperName, Pageable pageable);
}
