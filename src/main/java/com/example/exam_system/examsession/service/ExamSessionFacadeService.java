package com.example.exam_system.examsession.service;

import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.exampaper.entity.Paper;
import com.example.exam_system.exampaper.repository.PaperRepository;
import com.example.exam_system.examscore.entity.ExamRecord;
import com.example.exam_system.examscore.repository.ExamRecordRepository;
import com.example.exam_system.examscore.service.ExamScoreService;
import com.example.exam_system.examsession.dto.*;
import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examsession.entity.ExamSessionQuestion;
import com.example.exam_system.examsession.entity.ExamSessionAnswer;
import com.example.exam_system.examsession.repository.ExamSessionQuestionRepository;
import com.example.exam_system.examsession.repository.ExamSessionAnswerRepository;
import com.example.exam_system.examsession.repository.ExamSessionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamSessionFacadeService implements IExamSessionService {

    private static final Logger log = LoggerFactory.getLogger(ExamSessionFacadeService.class);

    @Autowired
    private ExamSessionLifecycleService lifecycleService;

    @Autowired
    private ExamPaperCopyService paperCopyService;

    @Autowired
    private AnswerInitializationService answerInitService;

    @Autowired
    private RedisAnswerCacheService redisCacheService;

    @Autowired
    private DatabaseAnswerPersistService dbPersistService;

    @Autowired
    private ExamSessionQuestionRepository sessionQuestionRepo;

    @Autowired
    private ExamSessionAnswerRepository sessionAnswerRepo;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamScoreService examScoreService;

    @Transactional
    public ExamStartResponseDTO startExam(Long examId) {
        ExamSession session = lifecycleService.createSession(examId);

        boolean isNewSession = sessionAnswerRepo.findBySessionId(session.getId()).isEmpty();

        if (isNewSession) {
            List<ExamSessionQuestion> questions = paperCopyService.copyPaperToSession(
                    session.getId(), session.getPaperId());

            List<Long> questionIds = questions.stream()
                    .map(ExamSessionQuestion::getQuestionId)
                    .toList();
            answerInitService.initializeAnswers(session.getId(), questionIds);

            ExamAnswerCache cache = new ExamAnswerCache();
            cache.setSessionId(session.getId());
            cache.setSessionToken(session.getSessionToken());
            cache.setExamId(examId);
            cache.setStudentId(session.getStudentId());
            cache.setStatus("ONGOING");
            cache.setAnswers(new HashMap<>());
            redisCacheService.initCache(cache);

            redisCacheService.updateExamStatus(session.getSessionToken(), "ONGOING");

            log.info("创建新考试会话，sessionId: {}, examId: {}, 题目数：{}", session.getId(), examId, questions.size());
        } else {
            log.info("恢复已有考试会话，sessionId: {}", session.getId());
        }

        return buildStartResponse(session);
    }

    public ExamSessionDTO getExamPaper(String sessionToken) {
        ExamSession session = lifecycleService.validateSession(sessionToken);

        List<ExamSessionQuestion> questions = sessionQuestionRepo
                .findBySessionIdOrderByDisplayOrder(session.getId());

        Map<Long, String> answerMap = loadAnswers(sessionToken, session.getId());

        List<ExamSessionQuestionDTO> questionDTOs = convertToQuestionDTOs(questions, answerMap);

        return buildExamSessionDTO(session, questionDTOs);
    }

    public void saveAnswer(String sessionToken, AnswerSubmitDTO submitDTO) {
        ExamSession session = lifecycleService.validateSession(sessionToken);

        redisCacheService.updateAnswer(sessionToken, submitDTO.getQuestionId(), submitDTO.getStudentAnswer());

        log.info("保存答案到 Redis，sessionId: {}, questionId: {}", session.getId(), submitDTO.getQuestionId());
    }

    public void batchSaveAnswers(String sessionToken, List<AnswerSubmitDTO> answers) {
        ExamSession session = lifecycleService.validateSession(sessionToken);

        Map<Long, String> answerMap = new HashMap<>();
        for (AnswerSubmitDTO dto : answers) {
            answerMap.put(dto.getQuestionId(), dto.getStudentAnswer());
        }

        redisCacheService.batchUpdateAnswers(sessionToken, answerMap);

        log.info("批量保存答案到 Redis，sessionId: {}, 题目数：{}", session.getId(), answers.size());
    }

    @Transactional
    public Map<String, Object> submitExam(String sessionToken) {
        ExamSession session = lifecycleService.validateSession(sessionToken);

        Long sessionId = session.getId();

        ExamAnswerCache cache = redisCacheService.getCache(sessionToken);
        if (cache != null && cache.getAnswers() != null) {
            dbPersistService.persistFromCache(sessionId, cache.getAnswers());
            log.info("从 Redis 持久化答案到数据库，sessionId: {}, 答案数：{}", sessionId, cache.getAnswers().size());
        }

        session.setStatus(ExamSession.SessionStatus.SUBMITTED);
        session.setEndTime(LocalDateTime.now());
        lifecycleService.updateSessionStatus(session, ExamSession.SessionStatus.SUBMITTED);

        redisCacheService.deleteCache(sessionToken);

        // 创建考试记录（即使还未批改）
        createExamRecordOnSubmit(session);

        log.info("考试已提交，sessionId: {}", sessionId);

        return new HashMap<>() {{
            put("success", true);
            put("message", "考试已提交，等待批改");
            put("sessionId", sessionId);
        }};
    }

    // 异步批改（在事务外执行）
    public void asyncGradeExam(Long sessionId) {
        try {
            log.info("开始异步批改客观题，sessionId: {}", sessionId);
            examScoreService.autoGradeObjectiveQuestions(sessionId);
            log.info("批改完成，sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("批改失败：{}", e.getMessage(), e);
        }
    }

    public ExamSessionDTO restoreExamState(String sessionToken) {
        return getExamPaper(sessionToken);
    }

    public Map<String, Object> checkOngoingExam(String studentId) {
        List<ExamSession> ongoingSessions = examSessionRepository.findActiveOngoingSessionsByStudentId(studentId, LocalDateTime.now());

        if (ongoingSessions.isEmpty()) {
            return null;
        }

        ExamSession session = ongoingSessions.get(0);

        Map<String, Object> result = new HashMap<>();
        result.put("hasOngoingExam", true);
        result.put("sessionId", session.getId());
        result.put("sessionToken", session.getSessionToken());
        result.put("examId", session.getExamId());
        result.put("startTime", session.getStartTime());
        result.put("expectedEndTime", session.getExpectedEndTime());

        return result;
    }

    private Map<Long, String> loadAnswers(String sessionToken, Long sessionId) {
        if (redisCacheService.hasCache(sessionToken)) {
            ExamAnswerCache cache = redisCacheService.getCache(sessionToken);
            if (cache != null && cache.getAnswers() != null && !cache.getAnswers().isEmpty()) {
                log.info("从 Redis 缓存加载答案，题目数：{}", cache.getAnswers().size());
                return cache.getAnswers();
            }
        }

        List<ExamSessionAnswer> answers = sessionAnswerRepo.findBySessionId(sessionId);
        Map<Long, String> answerMap = new HashMap<>();
        for (ExamSessionAnswer answer : answers) {
            if (answer.getStudentAnswer() != null && !answer.getStudentAnswer().isEmpty()) {
                answerMap.put(answer.getQuestionId(), answer.getStudentAnswer());
            }
        }

        log.info("从数据库加载答案，题目数：{}", answerMap.size());
        return answerMap;
    }

    private List<ExamSessionQuestionDTO> convertToQuestionDTOs(List<ExamSessionQuestion> questions,
                                                               Map<Long, String> answerMap) {
        List<ExamSessionQuestionDTO> dtos = new ArrayList<>();

        for (ExamSessionQuestion q : questions) {
            ExamSessionQuestionDTO dto = new ExamSessionQuestionDTO();
            dto.setQuestionId(q.getQuestionId());
            dto.setTemplateOrder(q.getTemplateOrder());
            dto.setDisplayOrder(q.getDisplayOrder());
            dto.setQuestionType(q.getQuestionType());
            dto.setQuestionContent(q.getQuestionContent());
            dto.setScore(q.getScore());
            dto.setStudentAnswer(answerMap.getOrDefault(q.getQuestionId(), ""));

            if (q.getOptionsJson() != null && !q.getOptionsJson().isEmpty()) {
                try {
                    List<ExamSessionQuestionDTO.OptionDTO> options = objectMapper.readValue(
                            q.getOptionsJson(),
                            new TypeReference<List<ExamSessionQuestionDTO.OptionDTO>>() {}
                    );
                    dto.setOptions(options);
                } catch (Exception e) {
                    log.error("解析选项JSON失败：{}", e.getMessage());
                    dto.setOptions(new ArrayList<>());
                }
            } else {
                dto.setOptions(new ArrayList<>());
            }

            dtos.add(dto);
        }

        return dtos;
    }

    private ExamSessionDTO buildExamSessionDTO(ExamSession session,
                                               List<ExamSessionQuestionDTO> questions) {
        ExamSessionDTO dto = new ExamSessionDTO();
        dto.setSessionId(session.getId());
        dto.setExamId(session.getExamId());
        dto.setSessionToken(session.getSessionToken());
        dto.setStatus(session.getStatus().name());
        dto.setStartTime(session.getStartTime());
        dto.setExpectedEndTime(session.getExpectedEndTime());
        dto.setDurationMinutes(calculateDurationMinutes(session));
        dto.setTotalScore(session.getTotalScore());
        dto.setObtainedScore(session.getObtainedScore());
        dto.setQuestions(questions);
        return dto;
    }

    private ExamStartResponseDTO buildStartResponse(ExamSession session) {
        ExamStartResponseDTO dto = new ExamStartResponseDTO();
        dto.setSessionId(session.getId());
        dto.setSessionToken(session.getSessionToken());
        dto.setExamId(session.getExamId());
        dto.setStartTime(session.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setExpectedEndTime(session.getExpectedEndTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setTotalScore(session.getTotalScore());
        return dto;
    }

    private Integer calculateDurationMinutes(ExamSession session) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(now, session.getExpectedEndTime()).toMinutes();
        return minutes > 0 ? (int) minutes : 0;
    }

    private void createExamRecordOnSubmit(ExamSession session) {
        try {
            Optional<ExamRecord> existingRecordOpt = examRecordRepository.findBySessionId(session.getId());

            if (existingRecordOpt.isPresent()) {
                log.info("考试记录已存在，跳过创建，sessionId: {}", session.getId());
                return;
            }

            ExamRecord record = new ExamRecord();
            record.setSessionId(session.getId());
            record.setStudentId(session.getStudentId());
            record.setStartTime(session.getStartTime());
            record.setEndTime(session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now());

            Exam exam = examRepository.findById(session.getExamId())
                    .orElseThrow(() -> new RuntimeException("考试不存在"));
            record.setExamId(exam.getId());
            record.setGroupId(exam.getGroupId());
            record.setTeacherId(exam.getCreatorId());
            record.setExamName(exam.getExamName());
            record.setPaperId(exam.getPaperId());
            record.setScheduledDurationMinutes(exam.getDurationMinutes());

            Paper paper = paperRepository.findById(exam.getPaperId())
                    .orElseThrow(() -> new RuntimeException("试卷不存在"));
            record.setMaxScore(paper.getTotalScore());

            record.setTotalScore(0.0);
            record.setObjectiveScore(0.0);
            record.setSubjectiveScore(0.0);
            record.setScorePercentage(0.0);
            record.setIsSubmitted(true);
            record.setSubmittedAt(session.getEndTime());
            record.setStatus(ExamRecord.RecordStatus.SUBMITTED);

            long actualDurationMinutes = java.time.Duration.between(
                    session.getStartTime(),
                    record.getEndTime()
            ).toMinutes();
            record.setActualDurationMinutes((int) actualDurationMinutes);

            examRecordRepository.save(record);

            log.info("考试提交时创建记录，sessionId: {}, recordId: {}", session.getId(), record.getId());
        } catch (Exception e) {
            log.error("创建考试记录失败：{}", e.getMessage(), e);
        }
    }
}
