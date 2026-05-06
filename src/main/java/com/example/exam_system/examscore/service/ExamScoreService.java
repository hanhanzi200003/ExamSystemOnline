package com.example.exam_system.examscore.service;

import com.example.exam_system.exampaper.entity.PaperAnswer;
import com.example.exam_system.exampaper.repository.PaperAnswerRepository;
import com.example.exam_system.examscore.dto.ManualGradeDTO;
import com.example.exam_system.examscore.dto.ReviewedPaperDTO;
import com.example.exam_system.examscore.dto.ScoreResultDTO;
import com.example.exam_system.examscore.entity.ExamScore;
import com.example.exam_system.examscore.entity.ExamScoreDetail;
import com.example.exam_system.examscore.repository.ExamScoreDetailRepository;
import com.example.exam_system.examscore.repository.ExamScoreRepository;
import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examsession.entity.ExamSessionAnswer;
import com.example.exam_system.examsession.entity.ExamSessionQuestion;
import com.example.exam_system.examsession.repository.ExamSessionAnswerRepository;
import com.example.exam_system.examsession.repository.ExamSessionQuestionRepository;
import com.example.exam_system.examsession.repository.ExamSessionRepository;
import com.example.exam_system.examscore.dto.GradingQuestionDTO;
import com.example.exam_system.examscore.entity.ExamRecord;
import com.example.exam_system.examscore.repository.ExamRecordRepository;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.exampaper.repository.PaperRepository;
import com.example.exam_system.exampaper.entity.Paper;
import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.examsession.entity.ExamSession;
import com.example.exam_system.examgroup.repository.GroupRepository;
import com.example.exam_system.examgroup.entity.Group;
import com.example.exam_system.login.repository.UserRepository;
import com.example.exam_system.login.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamScoreService implements IExamScoreService {

    private static final Logger log = LoggerFactory.getLogger(ExamScoreService.class);

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @Autowired
    private ExamScoreDetailRepository scoreDetailRepository;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamSessionQuestionRepository sessionQuestionRepository;

    @Autowired
    private ExamSessionAnswerRepository sessionAnswerRepository;

    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ScoreResultDTO autoGradeObjectiveQuestions(Long sessionId) {
        System.out.println("========================================");
        System.out.println("这里是批改开始 - sessionId: " + sessionId);
        System.out.println("========================================");

        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));

        // 检查是否已经存在 ExamScore 记录
        ExamScore examScore;
        boolean isNewScore = false;

        Optional<ExamScore> existingScoreOpt = examScoreRepository.findBySessionId(sessionId);
        if (existingScoreOpt.isPresent()) {
            examScore = existingScoreOpt.get();
            log.info("ExamScore 记录已存在，将更新 - sessionId: {}, scoreId: {}", sessionId, examScore.getId());
        } else {
            examScore = new ExamScore();
            examScore.setSessionId(sessionId);
            examScore.setExamId(session.getExamId());
            examScore.setStudentId(session.getStudentId());
            examScore.setObjectiveScore(0.0);
            examScore.setSubjectiveScore(0.0);
            examScore.setTotalScore(0.0);
            examScore.setGradedAt(LocalDateTime.now());
            examScore.setSubmittedAt(session.getEndTime());

            if (session.getStartTime() != null && session.getEndTime() != null) {
                long duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
                examScore.setExamDurationMinutes((int) duration);
            }

            Exam exam = examRepository.findById(session.getExamId())
                    .orElse(null);
            if (exam != null) {
                examScore.setExamName(exam.getExamName());
                examScore.setScheduledDurationMinutes(exam.getDurationMinutes());
                examScore.setEarliestStartTime(exam.getEarliestStartTime());

                groupRepository.findById(exam.getGroupId()).ifPresent(group -> {
                    examScore.setGroupName(group.getGroupName());
                });

                userRepository.findByUsername(exam.getCreatorId()).ifPresent(user -> {
                    String name = user.getNickname() != null ? user.getNickname() : user.getUsername();
                    examScore.setTeacherName(name);
                });

                paperRepository.findById(exam.getPaperId()).ifPresent(paper -> {
                    examScore.setMaxScore(paper.getTotalScore());
                });
            }

            examScoreRepository.save(examScore);
            isNewScore = true;
            log.info("创建新的 ExamScore 记录 - sessionId: {}, scoreId: {}", sessionId, examScore.getId());
        }

        Long scoreId = examScore.getId();

        List<ExamSessionQuestion> questions = sessionQuestionRepository.findBySessionIdOrderByTemplateOrder(sessionId);
        List<ExamSessionAnswer> answers = sessionAnswerRepository.findBySessionId(sessionId);

        log.info("=== 开始批改 === sessionId: {}, 题目数：{}, 答案数：{}", sessionId, questions.size(), answers.size());

        Map<Long, ExamSessionAnswer> answerMap = buildAnswerMap(answers);
        Map<Long, String> correctAnswerMap = loadCorrectAnswers(questions);

        log.info("=== 答案映射 === 学生答案数：{}, 正确答案数：{}", answerMap.size(), correctAnswerMap.size());

        double objectiveScore = 0.0;
        List<ExamScoreDetail> scoreDetails = new ArrayList<>();

        for (ExamSessionQuestion question : questions) {
            Long questionId = question.getQuestionId();
            log.info("处理题目 - questionId: {} (类型: {}), 题型: {}",
                    questionId, questionId != null ? questionId.getClass().getName() : "null", question.getQuestionType());

            ExamSessionAnswer studentAnswer = answerMap.get(questionId);
            String studentAnswerContent = studentAnswer != null ? studentAnswer.getStudentAnswer() : "";
            String correctAnswer = correctAnswerMap.get(questionId);

            log.info("题目 ID: {}, 题型：{}, 学生答案：'{}', 正确答案：'{}'",
                    questionId, question.getQuestionType(),
                    studentAnswerContent != null ? studentAnswerContent : "null",
                    correctAnswer != null ? correctAnswer : "null");

            log.info("answerMap 包含该 questionId? {}, correctAnswerMap 包含该 questionId? {}",
                    answerMap.containsKey(questionId), correctAnswerMap.containsKey(questionId));

            if (isObjectiveQuestion(question.getQuestionType())) {
                double scoreRatio = calculateScoreRatio(studentAnswerContent, correctAnswer, question.getQuestionType());
                double obtainedScore = question.getScore() * scoreRatio;
                boolean isCorrect = scoreRatio == 1.0;
                boolean isPartialCorrect = scoreRatio > 0 && scoreRatio < 1;

                log.info("批改结果：{}, 得分比例：{}, 得分：{}",
                        isCorrect ? "正确" : (isPartialCorrect ? "半对" : "错误"), scoreRatio, obtainedScore);

                objectiveScore += obtainedScore;

                ExamScoreDetail detail = createScoreDetail(scoreId, sessionId, question, studentAnswerContent,
                        correctAnswer, isCorrect, obtainedScore, true);
                detail.setIsPartialCorrect(isPartialCorrect);
                scoreDetails.add(detail);

                if (studentAnswer != null) {
                    studentAnswer.setIsCorrect(isCorrect);
                    studentAnswer.setScored(obtainedScore);
                    studentAnswer.setAutoGraded(true);
                    sessionAnswerRepository.save(studentAnswer);
                    log.info("已更新 ExamSessionAnswer - questionId: {}, isCorrect: {}, scored: {}",
                            questionId, isCorrect, obtainedScore);
                }
            } else {
                ExamScoreDetail detail = createScoreDetail(scoreId, sessionId, question, studentAnswerContent,
                        correctAnswer, false, -1.0, false);
                scoreDetails.add(detail);

                log.info("主观题 {} 已创建评分记录，等待批改", question.getId());
            }
        }

        examScore.setObjectiveScore(objectiveScore);
        examScore.setTotalScore(objectiveScore);
        examScoreRepository.save(examScore);

        // 如果是新创建的分数记录，保存所有详情；否则只更新客观题详情
        if (isNewScore) {
            scoreDetailRepository.saveAll(scoreDetails);
        } else {
            // 只更新客观题的详情
            for (ExamScoreDetail detail : scoreDetails) {
                if (detail.getAutoGraded()) {
                    scoreDetailRepository.save(detail);
                }
            }
        }

        createOrUpdateExamRecord(session, examScore, questions);

        log.info("=== 批改完成 === sessionId: {}, 客观题总分：{}", sessionId, objectiveScore);

        return buildScoreResultDTO(examScore);
    }

    @Override
    @Transactional
    public void manualGradeSubjectiveQuestions(Long sessionId, List<ManualGradeDTO> grades) {
        ExamScore examScore = examScoreRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("未找到成绩记录，请先批改客观题"));

        double subjectiveScore = 0.0;

        for (ManualGradeDTO grade : grades) {
            ExamScoreDetail detail = scoreDetailRepository.findBySessionId(sessionId).stream()
                    .filter(d -> d.getQuestionId().equals(grade.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("题目评分详情不存在"));

            if (detail.getQuestionScore() == null) {
                throw new RuntimeException("题目满分信息缺失");
            }

            Double scored = grade.getScored();
            if (scored == null) {
                throw new RuntimeException("评分不能为空");
            }

            if (scored < 0) {
                throw new RuntimeException("评分不能为负数");
            }

            if (scored > detail.getQuestionScore()) {
                throw new RuntimeException(String.format("评分不能超过题目满分 %.2f 分", detail.getQuestionScore()));
            }

            detail.setObtainedScore(scored);
            scoreDetailRepository.save(detail);

            subjectiveScore += scored;
        }

        List<ExamScoreDetail> allDetails = scoreDetailRepository.findBySessionId(sessionId);
        double totalSubjectiveScore = allDetails.stream()
                .filter(d -> d.getAutoGraded() != null && !d.getAutoGraded() && d.getObtainedScore() != null)
                .mapToDouble(ExamScoreDetail::getObtainedScore)
                .sum();

        examScore.setSubjectiveScore(totalSubjectiveScore);
        examScore.setTotalScore(examScore.getObjectiveScore() + totalSubjectiveScore);
        examScoreRepository.save(examScore);

        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));
        List<ExamSessionQuestion> questions = sessionQuestionRepository.findBySessionIdOrderByTemplateOrder(sessionId);

        createOrUpdateExamRecord(session, examScore, questions);

        log.info("主观题批改完成，sessionId: {}, 主观题得分：{}", sessionId, totalSubjectiveScore);
    }

    //重复批改
    @Override
    @Transactional(readOnly = true)
    public List<GradingQuestionDTO> getGradingQuestions(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));

        List<ExamSessionQuestion> questions = sessionQuestionRepository
                .findBySessionIdOrderByTemplateOrder(sessionId);

        Map<Long, ExamScoreDetail> detailMap = new HashMap<>();
        List<ExamScoreDetail> details = scoreDetailRepository.findBySessionId(sessionId);
        for (ExamScoreDetail detail : details) {
            detailMap.put(detail.getQuestionId(), detail);
        }

        Map<Long, PaperAnswer> answerMap = new HashMap<>();
        for (ExamSessionQuestion question : questions) {
            paperAnswerRepository.findByPaperQuestionId(question.getQuestionId())
                    .ifPresent(answer -> answerMap.put(question.getQuestionId(), answer));
        }

        List<GradingQuestionDTO> result = new ArrayList<>();
        for (ExamSessionQuestion question : questions) {
            if (!isObjectiveQuestion(question.getQuestionType())) {
                GradingQuestionDTO dto = new GradingQuestionDTO();
                dto.setQuestionId(question.getQuestionId());
                dto.setQuestionNumber(question.getTemplateOrder());
                dto.setQuestionType(question.getQuestionType());
                dto.setQuestionScore(question.getScore());
                dto.setQuestionContent(question.getQuestionContent());

                PaperAnswer paperAnswer = answerMap.get(question.getQuestionId());
                if (paperAnswer != null) {
                    dto.setCorrectAnswer(paperAnswer.getCorrectAnswer());
                    dto.setExplanation(paperAnswer.getAnalysis());
                }

                ExamScoreDetail detail = detailMap.get(question.getQuestionId());
                if (detail != null) {
                    dto.setStudentAnswer(detail.getStudentAnswer());
                    dto.setObtainedScore(detail.getObtainedScore());
                    dto.setIsGraded(detail.getObtainedScore() != null && detail.getObtainedScore() >= 0);
                    dto.setGradedAt(session.getEndTime());
                } else {
                    dto.setStudentAnswer("");
                    dto.setObtainedScore(0.0);
                    dto.setIsGraded(false);
                }

                result.add(dto);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public GradingQuestionDTO getSingleGradingQuestion(Long sessionId, Long questionId) {
        ExamSessionQuestion question = sessionQuestionRepository
                .findBySessionIdAndQuestionId(sessionId, questionId);

        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        if (isObjectiveQuestion(question.getQuestionType())) {
            throw new RuntimeException("该题目是客观题，不需要手动批改");
        }

        GradingQuestionDTO dto = new GradingQuestionDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setQuestionNumber(question.getTemplateOrder());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestionScore(question.getScore());
        dto.setQuestionContent(question.getQuestionContent());

        ExamScoreDetail detail = scoreDetailRepository.findBySessionId(sessionId).stream()
                .filter(d -> d.getQuestionId().equals(questionId))
                .findFirst()
                .orElse(null);

        if (detail != null) {
            dto.setStudentAnswer(detail.getStudentAnswer());
            dto.setObtainedScore(detail.getObtainedScore());
            dto.setIsGraded(detail.getObtainedScore() != null && detail.getObtainedScore() >= 0);
        } else {
            dto.setStudentAnswer("");
            dto.setObtainedScore(0.0);
            dto.setIsGraded(false);
        }

        return dto;
    }



    @Override
    @Transactional(readOnly = true)
    public ScoreResultDTO getScoreDetail(Long sessionId) {
        ExamScore examScore = examScoreRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("成绩不存在"));

        return buildScoreResultDTO(examScore);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewedPaperDTO getReviewedPaperForStudent(Long sessionId, String studentId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));

        if (!session.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权查看此试卷");
        }

        Exam exam = examRepository.findById(session.getExamId())
                .orElseThrow(() -> new RuntimeException("考试不存在"));

        Paper paper = paperRepository.findById(exam.getPaperId())
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        List<ExamSessionQuestion> questions = sessionQuestionRepository
                .findBySessionIdOrderByTemplateOrder(sessionId);

        List<ExamSessionAnswer> answers = sessionAnswerRepository.findBySessionId(sessionId);
        Map<Long, ExamSessionAnswer> answerMap = new HashMap<>();
        for (ExamSessionAnswer answer : answers) {
            answerMap.put(answer.getQuestionId(), answer);
        }

        // 加载分数详情
        List<ExamScoreDetail> scoreDetails = scoreDetailRepository.findBySessionId(sessionId);
        Map<Long, ExamScoreDetail> scoreDetailMap = new HashMap<>();
        for (ExamScoreDetail detail : scoreDetails) {
            scoreDetailMap.put(detail.getQuestionId(), detail);
        }

        Map<Long, String> correctAnswerMap = loadCorrectAnswers(questions);

        List<ReviewedPaperDTO.ReviewedQuestionDTO> reviewedQuestions = new ArrayList<>();
        for (ExamSessionQuestion question : questions) {
            ReviewedPaperDTO.ReviewedQuestionDTO reviewedQuestion =
                    new ReviewedPaperDTO.ReviewedQuestionDTO();

            reviewedQuestion.setQuestionId(question.getQuestionId());
            reviewedQuestion.setQuestionNumber(question.getTemplateOrder());
            reviewedQuestion.setQuestionType(question.getQuestionType());
            reviewedQuestion.setQuestionContent(question.getQuestionContent());
            reviewedQuestion.setQuestionScore(question.getScore());
            reviewedQuestion.setOptionsJson(question.getOptionsJson());

            ExamSessionAnswer answer = answerMap.get(question.getQuestionId());
            ExamScoreDetail scoreDetail = scoreDetailMap.get(question.getQuestionId());

            if (answer != null) {
                reviewedQuestion.setStudentAnswer(answer.getStudentAnswer());
            } else {
                reviewedQuestion.setStudentAnswer("");
            }

            // 从 ExamScoreDetail 获取分数，如果没有则从 ExamSessionAnswer 获取
            if (scoreDetail != null && scoreDetail.getObtainedScore() != null && scoreDetail.getObtainedScore() >= 0) {
                reviewedQuestion.setObtainedScore(scoreDetail.getObtainedScore());
                reviewedQuestion.setIsCorrect(scoreDetail.getIsCorrect());
                reviewedQuestion.setIsPartialCorrect(scoreDetail.getIsPartialCorrect());
            } else if (answer != null) {
                reviewedQuestion.setObtainedScore(answer.getScored());
                reviewedQuestion.setIsCorrect(answer.getIsCorrect());
                reviewedQuestion.setIsPartialCorrect(false);
            } else {
                reviewedQuestion.setObtainedScore(0.0);
                reviewedQuestion.setIsCorrect(false);
                reviewedQuestion.setIsPartialCorrect(false);
            }

            String correctAnswer = correctAnswerMap.get(question.getQuestionId());
            reviewedQuestion.setCorrectAnswer(correctAnswer != null ? correctAnswer : "");

            reviewedQuestions.add(reviewedQuestion);
        }

        reviewedQuestions.sort(Comparator.comparingInt(ReviewedPaperDTO.ReviewedQuestionDTO::getQuestionNumber));

        ReviewedPaperDTO reviewedPaper = new ReviewedPaperDTO();
        reviewedPaper.setSessionId(sessionId);
        reviewedPaper.setExamId(exam.getId());
        reviewedPaper.setExamName(exam.getExamName());
        reviewedPaper.setMaxScore(paper.getTotalScore());
        reviewedPaper.setSubmittedAt(session.getEndTime());

        ExamScore examScore = examScoreRepository.findBySessionId(sessionId).orElse(null);
        if (examScore != null) {
            reviewedPaper.setTotalScore(examScore.getTotalScore());
            reviewedPaper.setScorePercentage((examScore.getTotalScore() / paper.getTotalScore()) * 100);
        } else {
            reviewedPaper.setTotalScore(0.0);
            reviewedPaper.setScorePercentage(0.0);
        }

        reviewedPaper.setQuestions(reviewedQuestions);

        return reviewedPaper;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewedPaperDTO getReviewedPaperForTeacher(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));
        Exam exam = examRepository.findById(session.getExamId())
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        Paper paper = paperRepository.findById(exam.getPaperId())
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        List<ExamSessionQuestion> questions = sessionQuestionRepository
                .findBySessionIdOrderByTemplateOrder(sessionId);
        List<ExamSessionAnswer> answers = sessionAnswerRepository.findBySessionId(sessionId);
        Map<Long, ExamSessionAnswer> answerMap = new HashMap<>();
        for (ExamSessionAnswer answer : answers) {
            answerMap.put(answer.getQuestionId(), answer);
        }

        // 加载分数详情
        List<ExamScoreDetail> scoreDetails = scoreDetailRepository.findBySessionId(sessionId);
        Map<Long, ExamScoreDetail> scoreDetailMap = new HashMap<>();
        for (ExamScoreDetail detail : scoreDetails) {
            scoreDetailMap.put(detail.getQuestionId(), detail);
        }

        Map<Long, String> correctAnswerMap = loadCorrectAnswers(questions);
        List<ReviewedPaperDTO.ReviewedQuestionDTO> reviewedQuestions = new ArrayList<>();
        for (ExamSessionQuestion question : questions) {
            ReviewedPaperDTO.ReviewedQuestionDTO reviewedQuestion =
                    new ReviewedPaperDTO.ReviewedQuestionDTO();
            reviewedQuestion.setQuestionId(question.getQuestionId());
            reviewedQuestion.setQuestionNumber(question.getTemplateOrder());
            reviewedQuestion.setQuestionType(question.getQuestionType());
            reviewedQuestion.setQuestionContent(question.getQuestionContent());
            reviewedQuestion.setQuestionScore(question.getScore());
            reviewedQuestion.setOptionsJson(question.getOptionsJson());

            ExamSessionAnswer answer = answerMap.get(question.getQuestionId());
            ExamScoreDetail scoreDetail = scoreDetailMap.get(question.getQuestionId());

            if (answer != null) {
                reviewedQuestion.setStudentAnswer(answer.getStudentAnswer());
            } else {
                reviewedQuestion.setStudentAnswer("");
            }

            // 从 ExamScoreDetail 获取分数，如果没有则从 ExamSessionAnswer 获取
            if (scoreDetail != null && scoreDetail.getObtainedScore() != null && scoreDetail.getObtainedScore() >= 0) {
                reviewedQuestion.setObtainedScore(scoreDetail.getObtainedScore());
                reviewedQuestion.setIsCorrect(scoreDetail.getIsCorrect());
                reviewedQuestion.setIsPartialCorrect(scoreDetail.getIsPartialCorrect());
            } else if (answer != null) {
                reviewedQuestion.setObtainedScore(answer.getScored());
                reviewedQuestion.setIsCorrect(answer.getIsCorrect());
                reviewedQuestion.setIsPartialCorrect(false);
            } else {
                reviewedQuestion.setObtainedScore(0.0);
                reviewedQuestion.setIsCorrect(false);
                reviewedQuestion.setIsPartialCorrect(false);
            }

            String correctAnswer = correctAnswerMap.get(question.getQuestionId());
            reviewedQuestion.setCorrectAnswer(correctAnswer != null ? correctAnswer : "");
            reviewedQuestions.add(reviewedQuestion);
        }
        ReviewedPaperDTO reviewedPaper = new ReviewedPaperDTO();
        reviewedPaper.setSessionId(sessionId);
        reviewedPaper.setExamId(exam.getId());
        reviewedPaper.setExamName(exam.getExamName());
        reviewedPaper.setStudentId(session.getStudentId());

        Double maxScore = paper.getTotalScore() != null ? paper.getTotalScore() : 0.0;
        reviewedPaper.setMaxScore(maxScore);

        ExamScore examScore = examScoreRepository.findBySessionId(sessionId).orElse(null);
        if (examScore != null) {
            Double totalScore = examScore.getTotalScore() != null ? examScore.getTotalScore() : 0.0;
            Double objectiveScore = examScore.getObjectiveScore() != null ? examScore.getObjectiveScore() : 0.0;
            Double subjectiveScore = examScore.getSubjectiveScore() != null ? examScore.getSubjectiveScore() : 0.0;

            reviewedPaper.setTotalScore(totalScore);
            reviewedPaper.setObjectiveScore(objectiveScore);
            reviewedPaper.setSubjectiveScore(subjectiveScore);

            if (maxScore > 0) {
                reviewedPaper.setScorePercentage((totalScore / maxScore) * 100);
            } else {
                reviewedPaper.setScorePercentage(0.0);
            }
        } else {
            reviewedPaper.setTotalScore(0.0);
            reviewedPaper.setObjectiveScore(0.0);
            reviewedPaper.setSubjectiveScore(0.0);
            reviewedPaper.setScorePercentage(0.0);
        }
        reviewedPaper.setQuestions(reviewedQuestions);
        return reviewedPaper;
    }
    private ExamScoreDetail createScoreDetail(Long scoreId, Long sessionId, ExamSessionQuestion question,
                                              String studentAnswer, String correctAnswer,
                                              boolean isCorrect, Double obtainedScore, boolean autoGraded) {
        ExamScoreDetail detail = new ExamScoreDetail();
        detail.setScoreId(scoreId);
        detail.setSessionId(sessionId);
        detail.setQuestionId(question.getQuestionId());
        detail.setQuestionType(question.getQuestionType());
        detail.setQuestionScore(question.getScore());
        detail.setStudentAnswer(studentAnswer);
        detail.setCorrectAnswer(correctAnswer);
        detail.setIsCorrect(isCorrect);
        detail.setObtainedScore(obtainedScore);
        detail.setAutoGraded(autoGraded);
        return detail;
    }

    private Map<Long, ExamSessionAnswer> buildAnswerMap(List<ExamSessionAnswer> answers) {
        Map<Long, ExamSessionAnswer> map = new HashMap<>();
        for (ExamSessionAnswer answer : answers) {
            Long qid = answer.getQuestionId();
            log.info("学生答案映射 - questionId: {} (类型: {}), 学生答案: '{}'",
                    qid, qid != null ? qid.getClass().getName() : "null", answer.getStudentAnswer());
            map.put(qid, answer);
        }
        log.info("=== 学生答案映射结果 === 共 {} 条", map.size());
        return map;
    }

    private Map<Long, String> loadCorrectAnswers(List<ExamSessionQuestion> questions) {
        Map<Long, String> correctAnswerMap = new HashMap<>();

        for (ExamSessionQuestion question : questions) {
            paperAnswerRepository.findByPaperQuestionId(question.getQuestionId())
                    .ifPresent(answer ->
                            correctAnswerMap.put(question.getQuestionId(), answer.getCorrectAnswer())
                    );
        }

        return correctAnswerMap;
    }

    private boolean compareAnswers(String studentAnswer, String correctAnswer, String questionType) {
        if (studentAnswer == null || correctAnswer == null) {
            return false;
        }

        studentAnswer = studentAnswer.trim();
        correctAnswer = correctAnswer.trim();

        log.info("比较答案详情 - 题型: {}, 学生答案: '{}', 正确答案: '{}'", questionType, studentAnswer, correctAnswer);

        if ("TRUE_FALSE".equals(questionType)) {
            String normalizedStudentAnswer = normalizeTrueFalseAnswer(studentAnswer);
            String normalizedCorrectAnswer = normalizeTrueFalseAnswer(correctAnswer);
            boolean result = normalizedStudentAnswer.equals(normalizedCorrectAnswer);
            log.info("判断题比较结果: {}", result);
            return result;
        } else if ("SINGLE_CHOICE".equals(questionType)) {
            boolean result = studentAnswer.equalsIgnoreCase(correctAnswer);
            log.info("单选题比较结果: {}", result);
            return result;
        } else if ("MULTIPLE_CHOICE".equals(questionType)) {
            Set<String> studentSet = splitAndSort(studentAnswer);
            Set<String> correctSet = splitAndSort(correctAnswer);
            boolean result = studentSet.equals(correctSet);
            log.info("多选题比较结果: {}", result);
            return result;
        } else if ("FILL_BLANK".equals(questionType)) {
            boolean result = compareFillBlankAnswer(studentAnswer, correctAnswer);
            log.info("填空题比较结果: {}", result);
            return result;
        }

        boolean result = studentAnswer.equals(correctAnswer);
        log.info("其他题型比较结果: {}", result);
        return result;
    }

    private double calculateScoreRatio(String studentAnswer, String correctAnswer, String questionType) {
        if (studentAnswer == null || correctAnswer == null) {
            return 0.0;
        }

        studentAnswer = studentAnswer.trim();
        correctAnswer = correctAnswer.trim();

        if ("MULTIPLE_CHOICE".equals(questionType)) {
            Set<String> studentSet = splitAndSort(studentAnswer);
            Set<String> correctSet = splitAndSort(correctAnswer);

            if (studentSet.isEmpty() || correctSet.isEmpty()) {
                return 0.0;
            }

            if (studentSet.equals(correctSet)) {
                return 1.0;
            }

            Set<String> correctOnly = new HashSet<>(correctSet);
            correctOnly.removeAll(studentSet);

            Set<String> wrongAnswers = new HashSet<>(studentSet);
            wrongAnswers.removeAll(correctSet);

            if (wrongAnswers.isEmpty()) {
                int correctCount = correctSet.size();
                int studentCorrectCount = studentSet.size();
                if (studentCorrectCount < correctCount && studentCorrectCount > 0) {
                    return 0.5;
                }
                if (studentCorrectCount == correctCount) {
                    return 1.0;
                }
            }

            if (!wrongAnswers.isEmpty()) {
                return 0.0;
            }

            int correctCount = correctSet.size();
            int studentCorrectCount = correctSet.size() - correctOnly.size();
            if (correctCount > 0) {
                return (double) studentCorrectCount / correctCount;
            }
            return 0.0;
        } else if ("FILL_BLANK".equals(questionType)) {
            if (compareFillBlankAnswer(studentAnswer, correctAnswer)) {
                return 1.0;
            }
            return 0.0;
        } else if ("SINGLE_CHOICE".equals(questionType)) {
            if (studentAnswer.equalsIgnoreCase(correctAnswer)) {
                return 1.0;
            }
            return 0.0;
        } else if ("TRUE_FALSE".equals(questionType)) {
            String normalizedStudent = normalizeTrueFalseAnswer(studentAnswer);
            String normalizedCorrect = normalizeTrueFalseAnswer(correctAnswer);
            if (normalizedStudent.equals(normalizedCorrect)) {
                return 1.0;
            }
            return 0.0;
        }

        return 0.0;
    }

    private boolean compareFillBlankAnswer(String studentAnswer, String correctAnswer) {
        // 去除前后空格并忽略大小写
        String normalizedStudent = studentAnswer.trim().toLowerCase();
        String normalizedCorrect = correctAnswer.trim().toLowerCase();

        // 如果只有一个答案，直接比较
        if (!normalizedCorrect.contains(",") && !normalizedCorrect.contains("，")) {
            return normalizedStudent.equals(normalizedCorrect);
        }

        // 如果有多个正确答案（用逗号分隔），学生答案匹配任意一个即可
        String[] correctOptions = normalizedCorrect.split("[,，]");
        for (String option : correctOptions) {
            if (normalizedStudent.equals(option.trim())) {
                return true;
            }
        }

        return false;
    }

    private String normalizeTrueFalseAnswer(String answer) {
        if (answer == null) return "";

        String upperAnswer = answer.toUpperCase().trim();

        if ("A".equals(upperAnswer) || "正确".equals(answer) || "TRUE".equals(upperAnswer) || "对".equals(answer)) {
            return "正确";
        }
        if ("B".equals(upperAnswer) || "错误".equals(answer) || "FALSE".equals(upperAnswer) || "错".equals(answer)) {
            return "错误";
        }

        return answer;
    }

    private Set<String> splitAndSort(String answer) {
        String[] parts = answer.split("[,,]");
        Set<String> set = new HashSet<>();
        for (String part : parts) {
            set.add(part.trim().toUpperCase());
        }
        return set;
    }

    private boolean isObjectiveQuestion(String questionType) {
        return "SINGLE_CHOICE".equals(questionType) ||
                "MULTIPLE_CHOICE".equals(questionType) ||
                "TRUE_FALSE".equals(questionType) ||
                "FILL_BLANK".equals(questionType);
    }

    private boolean isSubjectiveQuestion(String questionType) {
        return "SHORT_ANSWER".equals(questionType);
    }

    private ScoreResultDTO buildScoreResultDTO(ExamScore examScore) {
        ScoreResultDTO dto = new ScoreResultDTO();
        dto.setSessionId(examScore.getSessionId());
        dto.setExamId(examScore.getExamId());
        dto.setStudentId(examScore.getStudentId());
        dto.setTotalScore(examScore.getTotalScore());
        dto.setObjectiveScore(examScore.getObjectiveScore());
        dto.setSubjectiveScore(examScore.getSubjectiveScore());
        dto.setGradedAt(examScore.getGradedAt());
        return dto;
    }

    private void createOrUpdateExamRecord(ExamSession session, ExamScore examScore, List<ExamSessionQuestion> questions) {
        try {
            Optional<ExamRecord> existingRecordOpt = examRecordRepository.findBySessionId(session.getId());

            ExamRecord record = existingRecordOpt.orElseGet(ExamRecord::new);
            String groupName = null;
            String teacherName = null;

            if (!existingRecordOpt.isPresent()) {
                record.setSessionId(session.getId());
                record.setStudentId(session.getStudentId());
                record.setStartTime(session.getStartTime());

                Exam exam = examRepository.findById(session.getExamId())
                        .orElseThrow(() -> new RuntimeException("考试不存在"));
                record.setExamId(exam.getId());
                record.setGroupId(exam.getGroupId());
                record.setTeacherId(exam.getCreatorId());
                record.setExamName(exam.getExamName());
                record.setPaperId(exam.getPaperId());
                record.setScheduledDurationMinutes(exam.getDurationMinutes());
                record.setEarliestStartTime(exam.getEarliestStartTime());

                groupRepository.findById(exam.getGroupId()).ifPresent(group -> {
                    record.setGroupName(group.getGroupName());
                });

                userRepository.findByUsername(exam.getCreatorId()).ifPresent(user -> {
                    String name = user.getNickname() != null ? user.getNickname() : user.getUsername();
                    record.setTeacherName(name);
                });

                Paper paper = paperRepository.findById(exam.getPaperId())
                        .orElseThrow(() -> new RuntimeException("试卷不存在"));
                record.setMaxScore(paper.getTotalScore());
            }

            record.setEndTime(session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now());
            record.setTotalScore(examScore.getTotalScore());
            record.setObjectiveScore(examScore.getObjectiveScore());
            record.setSubjectiveScore(examScore.getSubjectiveScore());
            record.setIsSubmitted(true);
            record.setSubmittedAt(session.getEndTime());

            if (record.getMaxScore() != null && record.getMaxScore() > 0 && examScore.getTotalScore() != null) {
                record.setScorePercentage((examScore.getTotalScore() / record.getMaxScore()) * 100);
            } else {
                record.setScorePercentage(0.0);
            }

            boolean hasSubjective = hasSubjectiveQuestions(session.getId());
            boolean fullyGraded = isFullyGraded(session.getId());

            if (!hasSubjective || fullyGraded) {
                record.setStatus(ExamRecord.RecordStatus.GRADED);
                record.setGradedAt(LocalDateTime.now());
            } else {
                record.setStatus(ExamRecord.RecordStatus.SUBMITTED);
            }

            long actualDurationMinutes = java.time.Duration.between(
                    session.getStartTime(),
                    record.getEndTime()
            ).toMinutes();
            record.setActualDurationMinutes((int) actualDurationMinutes);

            examRecordRepository.save(record);

            log.info("考试记录已保存/更新，sessionId: {}, recordId: {}", session.getId(), record.getId());
        } catch (Exception e) {
            log.error("创建考试记录失败：{}", e.getMessage(), e);
        }
    }

    @Override
    public boolean hasSubjectiveQuestions(Long sessionId) {
        List<ExamSessionQuestion> questions = sessionQuestionRepository.findBySessionIdOrderByTemplateOrder(sessionId);
        return questions.stream()
                .anyMatch(q -> !isObjectiveQuestion(q.getQuestionType()));
    }

    @Override
    public boolean isFullyGraded(Long sessionId) {
        List<ExamSessionQuestion> questions = sessionQuestionRepository.findBySessionIdOrderByTemplateOrder(sessionId);
        List<ExamScoreDetail> details = scoreDetailRepository.findBySessionId(sessionId);

        Map<Long, ExamScoreDetail> detailMap = new HashMap<>();
        for (ExamScoreDetail detail : details) {
            detailMap.put(detail.getQuestionId(), detail);
        }

        for (ExamSessionQuestion question : questions) {
            if (!isObjectiveQuestion(question.getQuestionType())) {
                ExamScoreDetail detail = detailMap.get(question.getQuestionId());
                if (detail == null || detail.getObtainedScore() == null || detail.getObtainedScore() < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getPendingGradingStudents(Long examId) {
        List<ExamSession> sessions = examSessionRepository.findByExamId(examId);
        List<Map<String, Object>> pendingStudents = new ArrayList<>();

        log.info("=== 查询待批改学生 === examId: {}, 总会话数: {}", examId, sessions.size());

        for (ExamSession session : sessions) {
            log.info("检查会话 - sessionId: {}, status: {}, hasSubjective: {}",
                    session.getId(), session.getStatus(), hasSubjectiveQuestions(session.getId()));

            if (session.getStatus() == ExamSession.SessionStatus.SUBMITTED) {
                boolean hasSubjective = hasSubjectiveQuestions(session.getId());

                if (hasSubjective) {
                    boolean fullyGraded = isFullyGraded(session.getId());
                    log.info("批改状态检查 - sessionId: {}, hasSubjective: {}, fullyGraded: {}",
                            session.getId(), hasSubjective, fullyGraded);

                    if (!fullyGraded) {
                        Map<String, Object> studentInfo = new HashMap<>();
                        studentInfo.put("sessionId", session.getId());
                        studentInfo.put("studentId", session.getStudentId());
                        studentInfo.put("submittedAt", session.getEndTime());
                        studentInfo.put("status", "PENDING");
                        pendingStudents.add(studentInfo);
                        log.info("添加到待批改列表 - sessionId: {}, studentId: {}",
                                session.getId(), session.getStudentId());
                    }
                }
            }
        }

        log.info("=== 待批改学生查询完成 === examId: {}, 待批改学生数: {}", examId, pendingStudents.size());
        return pendingStudents;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getGradingStats(Long examId) {
        Map<String, Object> stats = new HashMap<>();

        List<ExamSession> sessions = examSessionRepository.findByExamId(examId);

        int totalSubmitted = 0;
        int fullyGraded = 0;
        int pendingGrading = 0;

        for (ExamSession session : sessions) {
            if (session.getStatus() == ExamSession.SessionStatus.SUBMITTED) {
                totalSubmitted++;

                boolean hasSubjective = hasSubjectiveQuestions(session.getId());

                if (hasSubjective) {
                    if (isFullyGraded(session.getId())) {
                        fullyGraded++;
                    } else {
                        pendingGrading++;
                    }
                } else {
                    fullyGraded++;
                }
            }
        }

        stats.put("totalSubmitted", totalSubmitted);
        stats.put("fullyGraded", fullyGraded);
        stats.put("pendingGrading", pendingGrading);

        return stats;
    }

}
