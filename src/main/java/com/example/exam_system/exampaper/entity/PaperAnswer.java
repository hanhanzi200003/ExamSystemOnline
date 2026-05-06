package com.example.exam_system.exampaper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "exam_paper_answers")
public class PaperAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paper_question_id", nullable = false)
    private Long paperQuestionId;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis;
}
