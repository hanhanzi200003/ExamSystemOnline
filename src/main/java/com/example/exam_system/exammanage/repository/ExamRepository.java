package com.example.exam_system.exammanage.repository;

import com.example.exam_system.exammanage.entity.Exam;
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
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByGroupId(Long groupId);
    boolean existsByGroupIdAndCreatorId(Long groupId, String creatorId);

    boolean existsByPaperId(Long paperId);

    Optional<Exam> findByPaperId(Long paperId);

    List<Exam> findByCreatorId(String creatorId);

    Page<Exam> findByExamNameContaining(String examName, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Exam e WHERE e.creatorId = :creatorId")
    void deleteByCreatorId(@Param("creatorId") String creatorId);
}
