// src/main/java/com/example/exam_system/examquestionbank/entity/Answer.java
package com.example.exam_system.examquestionbank.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "question_bank_answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId; // 关联题目ID

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer; // 标准答案

    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis; // 解析说明
}
