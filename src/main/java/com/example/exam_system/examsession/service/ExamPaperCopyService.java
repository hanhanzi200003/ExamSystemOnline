package com.example.exam_system.examsession.service;

import com.example.exam_system.exampaper.entity.PaperOption;
import com.example.exam_system.exampaper.entity.PaperQuestion;
import com.example.exam_system.exampaper.repository.PaperOptionRepository;
import com.example.exam_system.exampaper.repository.PaperQuestionRepository;
import com.example.exam_system.examsession.dto.ExamSessionQuestionDTO;
import com.example.exam_system.examsession.entity.ExamSessionQuestion;
import com.example.exam_system.examsession.repository.ExamSessionQuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class ExamPaperCopyService {

    private static final Logger log = LoggerFactory.getLogger(ExamPaperCopyService.class);

    @Autowired
    private PaperQuestionRepository paperQuestionRepo;

    @Autowired
    private PaperOptionRepository paperOptionRepo;

    @Autowired
    private ExamSessionQuestionRepository sessionQuestionRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public List<ExamSessionQuestion> copyPaperToSession(Long sessionId, Long paperId) {
        List<PaperQuestion> templateQuestions = paperQuestionRepo.findByPaperIdOrderByQuestionNumber(paperId);

        if (templateQuestions.isEmpty()) {
            throw new RuntimeException("试卷中没有题目");
        }

        List<Integer> displayOrders = generateRandomDisplayOrders(templateQuestions.size());

        List<ExamSessionQuestion> sessionQuestions = new ArrayList<>();
        int index = 0;

        for (PaperQuestion pq : templateQuestions) {
            ExamSessionQuestion sq = createSessionQuestion(sessionId, pq, displayOrders.get(index++));
            sessionQuestions.add(sq);
        }

        List<ExamSessionQuestion> saved = sessionQuestionRepo.saveAll(sessionQuestions);
        log.info("从试卷模板拷贝 {} 道题目到会话 {}, 展示顺序已随机打乱", templateQuestions.size(), sessionId);
        return saved;
    }

    private ExamSessionQuestion createSessionQuestion(Long sessionId, PaperQuestion pq, Integer displayOrder) {
        ExamSessionQuestion sq = new ExamSessionQuestion();
        sq.setSessionId(sessionId);
        sq.setQuestionId(pq.getId());
        sq.setTemplateOrder(pq.getQuestionNumber());
        sq.setDisplayOrder(displayOrder);
        sq.setQuestionType(pq.getQuestionType());
        sq.setQuestionContent(pq.getQuestionContentSnapshot());
        sq.setScore(pq.getScore());

        try {
            List<PaperOption> options = paperOptionRepo.findByPaperQuestionId(pq.getId());
            List<ExamSessionQuestionDTO.OptionDTO> optionDTOs = new ArrayList<>();
            for (PaperOption opt : options) {
                ExamSessionQuestionDTO.OptionDTO dto = new ExamSessionQuestionDTO.OptionDTO();
                dto.setId(opt.getId());
                dto.setOptionLabel(opt.getOptionLabel());
                dto.setOptionContent(opt.getOptionContent());
                dto.setIsCorrect(opt.getIsCorrect());
                optionDTOs.add(dto);
            }
            sq.setOptionsJson(objectMapper.writeValueAsString(optionDTOs));
        } catch (JsonProcessingException e) {
            log.error("选项序列化失败：{}", e.getMessage());
        }

        return sq;
    }

    private List<Integer> generateRandomDisplayOrders(int size) {
        List<Integer> orders = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            orders.add(i);
        }
        Collections.shuffle(orders, new Random(System.currentTimeMillis()));
        log.debug("生成随机展示顺序：{}", orders);
        return orders;
    }
}
