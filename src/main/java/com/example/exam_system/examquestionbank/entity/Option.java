// src/main/java/com/example/exam_system/examquestionbank/entity/Option.java
package com.example.exam_system.examquestionbank.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "question_bank_options")
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId; // 关联题目ID

    @Column(name = "option_label", length = 10, nullable = false)
    private String optionLabel; // 选项标识(A/B/C/D)

    @Column(name = "option_content", columnDefinition = "TEXT", nullable = false)
    private String optionContent; // 选项内容

    @Column(name = "is_correct")
    private Boolean isCorrect = false; // 是否为正确答案
}
