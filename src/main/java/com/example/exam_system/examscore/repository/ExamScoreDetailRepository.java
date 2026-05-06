package com.example.exam_system.examscore.repository;

import com.example.exam_system.examscore.entity.ExamScoreDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamScoreDetailRepository extends JpaRepository<ExamScoreDetail, Long> {
    List<ExamScoreDetail> findBySessionId(Long sessionId);

    List<ExamScoreDetail> findByScoreId(Long scoreId);

    @Modifying
    @Query("DELETE FROM ExamScoreDetail esd WHERE esd.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
