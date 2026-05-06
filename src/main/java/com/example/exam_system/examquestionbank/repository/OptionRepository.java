// src/main/java/com/example/exam_system/examquestionbank/repository/OptionRepository.java
package com.example.exam_system.examquestionbank.repository;

import com.example.exam_system.examquestionbank.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByQuestionId(Long questionId);
    void deleteByQuestionId(Long questionId);
    void deleteByQuestionIdIn(List<Long> questionIds);
}
