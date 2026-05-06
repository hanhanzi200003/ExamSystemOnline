package com.example.exam_system.exampaper.service;

import com.example.exam_system.exampaper.dto.*;
import com.example.exam_system.exampaper.entity.*;
import com.example.exam_system.exampaper.enums.PaperType;
import com.example.exam_system.exampaper.repository.*;
import com.example.exam_system.examquestionbank.dto.QuestionDTO;
import com.example.exam_system.examquestionbank.service.QuestionBankService;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.login.service.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PaperService {

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Autowired
    private PaperOptionRepository paperOptionRepository;

    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private ExamRepository examRepository;

    /**
     * 手动组卷
     */
    public PaperDetailDTO createManualPaper(ManualPaperCreateDTO dto) {
        String creatorId = userContextService.getCurrentUserId();

        if (dto.getQuestions() == null || dto.getQuestions().isEmpty()) {
            throw new RuntimeException("必须至少选择一道题目");
        }

        double totalScore = 0;
        for (ManualPaperCreateDTO.QuestionSelectionDTO selection : dto.getQuestions()) {
            if (selection.getScore() == null || selection.getScore() <= 0) {
                throw new RuntimeException("每题分数必须大于 0");
            }
            totalScore += selection.getScore();
        }

        Paper paper = new Paper();
        paper.setPaperName(dto.getPaperName());
        paper.setCreatorId(creatorId);
        paper.setTotalScore(totalScore);
        paper.setPaperType(PaperType.MANUAL.name());

        Paper savedPaper = paperRepository.save(paper);

        int questionNumber = 1;
        for (ManualPaperCreateDTO.QuestionSelectionDTO selection : dto.getQuestions()) {
            savePaperQuestionWithSnapshot(savedPaper.getId(), questionNumber++, selection.getQuestionId(), selection.getScore());
        }

        return getPaperDetail(savedPaper.getId(), creatorId);
    }

    /**
     * 自动组卷
     */
    public PaperDetailDTO createAutoPaper(AutoPaperCreateDTO dto) {
        String creatorId = userContextService.getCurrentUserId();

        if (dto.getTypeConfigs() == null || dto.getTypeConfigs().isEmpty()) {
            throw new RuntimeException("必须至少配置一种题型");
        }

        double totalScore = 0;
        int totalQuestions = 0;
        StringBuilder warningMsg = new StringBuilder();

        for (AutoPaperCreateDTO.TypeConfigDTO config : dto.getTypeConfigs()) {
            if (config.getCount() == null || config.getCount() <= 0) {
                throw new RuntimeException(config.getQuestionType() + " 的题目数量必须大于 0");
            }
            if (config.getScorePerQuestion() == null || config.getScorePerQuestion() <= 0) {
                throw new RuntimeException(config.getQuestionType() + " 的每题分数必须大于 0");
            }
        }

        Paper paper = new Paper();
        paper.setPaperName(dto.getPaperName());
        paper.setCreatorId(creatorId);
        paper.setPaperType(PaperType.AUTO.name());
        paper.setTotalScore(0.0);  // 初始化总分，防止数据库约束错误

        Paper savedPaper = paperRepository.save(paper);

        int questionNumber = 1;
        for (AutoPaperCreateDTO.TypeConfigDTO config : dto.getTypeConfigs()) {
            List<QuestionDTO> questions = questionBankService.getRandomQuestionsByType(
                    creatorId, config.getQuestionType(), config.getCount());

            int requestedCount = config.getCount();
            int actualCount = questions.size();

            if (actualCount < requestedCount) {
                if (warningMsg.length() > 0) {
                    warningMsg.append("；");
                }
                warningMsg.append(String.format("%s题库题目不足：需要%d道，实际抽取%d道",
                        config.getQuestionType(), requestedCount, actualCount));
            }

            double actualScorePerQuestion = config.getScorePerQuestion();
            double typeTotalScore = actualScorePerQuestion * actualCount;
            totalScore += typeTotalScore;
            totalQuestions += actualCount;

            for (QuestionDTO question : questions) {
                savePaperQuestionFromDTO(savedPaper.getId(), questionNumber++, question, actualScorePerQuestion);
            }
        }

        paper.setTotalScore(totalScore);
        paperRepository.save(paper);

        PaperDetailDTO result = getPaperDetail(savedPaper.getId(), creatorId);

        if (warningMsg.length() > 0) {
            result.setWarningMessage(warningMsg.toString());
        }

        return result;
    }

    /**
     * 获取试卷详情
     */
    @Transactional(readOnly = true)
    public PaperDetailDTO getPaperDetail(Long paperId, String requesterId) {
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        if (!paper.getCreatorId().equals(requesterId)) {
            throw new RuntimeException("无权访问该试卷");
        }

        PaperDetailDTO detailDTO = convertToDetailDTO(paper);
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperIdOrderByQuestionNumber(paperId);

        List<PaperDetailDTO.PaperQuestionDTO> questionDTOs = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            PaperDetailDTO.PaperQuestionDTO questionDTO = new PaperDetailDTO.PaperQuestionDTO();
            questionDTO.setId(pq.getId());
            questionDTO.setQuestionNumber(pq.getQuestionNumber());
            questionDTO.setScore(pq.getScore());
            questionDTO.setQuestionType(pq.getQuestionType());
            questionDTO.setQuestionContent(pq.getQuestionContentSnapshot());

            List<PaperOption> options = paperOptionRepository.findByPaperQuestionId(pq.getId());
            List<PaperDetailDTO.PaperQuestionDTO.OptionDTO> optionDTOs = new ArrayList<>();
            for (PaperOption option : options) {
                PaperDetailDTO.PaperQuestionDTO.OptionDTO optionDTO = new PaperDetailDTO.PaperQuestionDTO.OptionDTO();
                optionDTO.setOptionLabel(option.getOptionLabel());
                optionDTO.setOptionContent(option.getOptionContent());
                optionDTO.setIsCorrect(option.getIsCorrect());
                optionDTOs.add(optionDTO);
            }
            questionDTO.setOptions(optionDTOs);

            paperAnswerRepository.findByPaperQuestionId(pq.getId()).ifPresent(answer -> {
                questionDTO.setCorrectAnswer(answer.getCorrectAnswer());
                questionDTO.setAnalysis(answer.getAnalysis());
            });

            questionDTOs.add(questionDTO);
        }

        detailDTO.setQuestions(questionDTOs);
        return detailDTO;
    }

    /**
     * 获取教师的所有试卷列表
     */
    @Transactional(readOnly = true)
    public List<PaperListDTO> getPapersByTeacher(String teacherId) {
        List<Paper> papers = paperRepository.findByCreatorId(teacherId);
        return papers.stream()
                .map(this::convertToListDTO)
                .toList();
    }

    /**
     * 删除试卷
     */
    public void deletePaper(Long paperId, String deleterId) {
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        if (!paper.getCreatorId().equals(deleterId)) {
            throw new RuntimeException("无权删除该试卷");
        }

        if (examRepository.existsByPaperId(paperId)) {
            throw new RuntimeException("该试卷已被考试引用，无法删除。请先删除相关考试后再删除试卷。");
        }

        deletePaperWithRelations(paperId);
    }

    /**
     * 修改试卷（先删除旧试卷，然后创建新试卷）
     * @param oldPaperId 旧试卷 ID
     * @param newPaperDTO 新试卷信息
     * @param operatorId 操作人 ID
     * @return 新创建的试卷详情
     */
    public PaperDetailDTO updatePaperByReplacement(Long oldPaperId, ManualPaperCreateDTO newPaperDTO, String operatorId) {
        Paper oldPaper = paperRepository.findById(oldPaperId)
                .orElseThrow(() -> new RuntimeException("旧试卷不存在"));

        if (!oldPaper.getCreatorId().equals(operatorId)) {
            throw new RuntimeException("无权修改该试卷");
        }

        if (examRepository.existsByPaperId(oldPaperId)) {
            throw new RuntimeException("该试卷已被考试引用，无法修改。请先删除相关考试后再修改试卷。");
        }

        // 验证新试卷数据
        if (newPaperDTO.getQuestions() == null || newPaperDTO.getQuestions().isEmpty()) {
            throw new RuntimeException("必须至少选择一道题目");
        }
        for (ManualPaperCreateDTO.QuestionSelectionDTO selection : newPaperDTO.getQuestions()) {
            if (selection.getScore() == null || selection.getScore() <= 0) {
                throw new RuntimeException("每题分数必须大于 0");
            }
        }

        deletePaperWithRelations(oldPaperId);

        return createManualPaper(newPaperDTO);
    }

    /**
     * 删除试卷及其关联的题目、选项、答案
     */
    private void deletePaperWithRelations(Long paperId) {
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperIdOrderByQuestionNumber(paperId);
        for (PaperQuestion pq : paperQuestions) {
            paperOptionRepository.deleteByPaperQuestionId(pq.getId());
            paperAnswerRepository.deleteByPaperQuestionId(pq.getId());
        }
        paperQuestionRepository.deleteByPaperId(paperId);
        paperRepository.deleteById(paperId);
    }

    /**
     * 保存试卷题目（从题库题目复制快照）
     */
    private void savePaperQuestionWithSnapshot(Long paperId, int questionNumber, Long questionId, Double score) {
        QuestionDTO questionDTO = questionBankService.getQuestionById(questionId, userContextService.getCurrentUserId());

        PaperQuestion paperQuestion = new PaperQuestion();
        paperQuestion.setPaperId(paperId);
        paperQuestion.setQuestionNumber(questionNumber);
        paperQuestion.setScore(score);
        paperQuestion.setQuestionContentSnapshot(questionDTO.getContent());
        paperQuestion.setQuestionType(questionDTO.getQuestionType().name());
        paperQuestion.setOriginalQuestionId(questionId);

        PaperQuestion savedPQ = paperQuestionRepository.save(paperQuestion);

        if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
            for (QuestionDTO.OptionDTO optionDTO : questionDTO.getOptions()) {
                PaperOption option = new PaperOption();
                option.setPaperQuestionId(savedPQ.getId());
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOptionContent(optionDTO.getOptionContent());
                option.setIsCorrect(optionDTO.getIsCorrect());
                paperOptionRepository.save(option);
            }
        }

        if (questionDTO.getCorrectAnswer() != null) {
            PaperAnswer answer = new PaperAnswer();
            answer.setPaperQuestionId(savedPQ.getId());
            answer.setCorrectAnswer(questionDTO.getCorrectAnswer());
            answer.setAnalysis(questionDTO.getAnalysis() != null ? questionDTO.getAnalysis() : "");
            paperAnswerRepository.save(answer);
        }
    }

    /**
     * 保存试卷题目（从 DTO 直接复制）
     */
    private void savePaperQuestionFromDTO(Long paperId, int questionNumber, QuestionDTO questionDTO, Double score) {
        PaperQuestion paperQuestion = new PaperQuestion();
        paperQuestion.setPaperId(paperId);
        paperQuestion.setQuestionNumber(questionNumber);
        paperQuestion.setScore(score);
        paperQuestion.setQuestionContentSnapshot(questionDTO.getContent());
        paperQuestion.setQuestionType(questionDTO.getQuestionType().name());
        paperQuestion.setOriginalQuestionId(questionDTO.getId());

        PaperQuestion savedPQ = paperQuestionRepository.save(paperQuestion);

        if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
            for (QuestionDTO.OptionDTO optionDTO : questionDTO.getOptions()) {
                PaperOption option = new PaperOption();
                option.setPaperQuestionId(savedPQ.getId());
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOptionContent(optionDTO.getOptionContent());
                option.setIsCorrect(optionDTO.getIsCorrect());
                paperOptionRepository.save(option);
            }
        }

        if (questionDTO.getCorrectAnswer() != null) {
            PaperAnswer answer = new PaperAnswer();
            answer.setPaperQuestionId(savedPQ.getId());
            answer.setCorrectAnswer(questionDTO.getCorrectAnswer());
            answer.setAnalysis(questionDTO.getAnalysis() != null ? questionDTO.getAnalysis() : "");
            paperAnswerRepository.save(answer);
        }
    }

    /**
     * 转换为详情 DTO
     */
    private PaperDetailDTO convertToDetailDTO(Paper paper) {
        PaperDetailDTO dto = new PaperDetailDTO();
        dto.setId(paper.getId());
        dto.setPaperName(paper.getPaperName());
        dto.setCreatorId(paper.getCreatorId());
        dto.setTotalScore(paper.getTotalScore());
        dto.setPaperType(paper.getPaperType());
        dto.setCreatedAt(paper.getCreatedAt());
        return dto;
    }

    private PaperListDTO convertToListDTO(Paper paper) {
        PaperListDTO dto = new PaperListDTO();
        dto.setId(paper.getId());
        dto.setPaperName(paper.getPaperName());
        dto.setTotalScore(paper.getTotalScore());
        dto.setPaperType(paper.getPaperType());
        dto.setCreatedAt(paper.getCreatedAt());
        return dto;
    }
}
