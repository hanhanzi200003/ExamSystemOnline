package com.example.exam_system.exampaper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exam_paper_questions")
public class PaperQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "question_content_snapshot", columnDefinition = "TEXT", nullable = false)
    private String questionContentSnapshot;

    @Column(name = "question_type", nullable = false)
    private String questionType;

    @Column(name = "original_question_id")
    private Long originalQuestionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
