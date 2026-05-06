package com.example.exam_system.exampaper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "exam_paper_options")
public class PaperOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paper_question_id", nullable = false)
    private Long paperQuestionId;

    @Column(name = "option_label", length = 10, nullable = false)
    private String optionLabel;

    @Column(name = "option_content", columnDefinition = "TEXT", nullable = false)
    private String optionContent;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;
}
