// src/main/java/com/example/exam_system/examquestionbank/service/QuestionBankService.java
package com.example.exam_system.examquestionbank.service;

import com.example.exam_system.examquestionbank.dto.QuestionCreateDTO;
import com.example.exam_system.examquestionbank.dto.QuestionDTO;
import com.example.exam_system.examquestionbank.entity.Answer;
import com.example.exam_system.examquestionbank.entity.Option;
import com.example.exam_system.examquestionbank.entity.Question;
import com.example.exam_system.examquestionbank.enums.QuestionType;
import com.example.exam_system.examquestionbank.repository.AnswerRepository;
import com.example.exam_system.examquestionbank.repository.OptionRepository;
import com.example.exam_system.examquestionbank.repository.QuestionBankRepository;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankService.class);

    @Autowired
    private QuestionBankRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    /**
     * 创建题目
     */
    @Transactional
    public QuestionDTO createQuestion(QuestionCreateDTO createDTO, String creatorId) {
        // 创建题目主表
        Question question = new Question();
        question.setContent(createDTO.getContent());
        question.setQuestionType(createDTO.getQuestionType());
        question.setCreatorId(creatorId);

        Question savedQuestion = questionRepository.save(question);

        // 处理选项（仅限选择题）
        if ((createDTO.getQuestionType() == QuestionType.SINGLE_CHOICE ||
                createDTO.getQuestionType() == QuestionType.MULTIPLE_CHOICE) &&
                createDTO.getOptions() != null) {

            for (QuestionCreateDTO.OptionDTO optionDTO : createDTO.getOptions()) {
                Option option = new Option();
                option.setQuestionId(savedQuestion.getId());
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOptionContent(optionDTO.getOptionContent());
                option.setIsCorrect(optionDTO.getIsCorrect());
                optionRepository.save(option);
            }
        }

        // 处理判断题选项（自动生成"正确"和"错误"两个选项）
        if (createDTO.getQuestionType() == QuestionType.TRUE_FALSE) {
            String correctAnswer = createDTO.getCorrectAnswer();
            
            Option correctOption = new Option();
            correctOption.setQuestionId(savedQuestion.getId());
            correctOption.setOptionLabel("A");
            correctOption.setOptionContent("正确");
            correctOption.setIsCorrect("正确".equals(correctAnswer));
            optionRepository.save(correctOption);

            Option wrongOption = new Option();
            wrongOption.setQuestionId(savedQuestion.getId());
            wrongOption.setOptionLabel("B");
            wrongOption.setOptionContent("错误");
            wrongOption.setIsCorrect("错误".equals(correctAnswer));
            optionRepository.save(wrongOption);
        }

        // 创建答案
        Answer answer = new Answer();
        answer.setQuestionId(savedQuestion.getId());
        answer.setCorrectAnswer(createDTO.getCorrectAnswer());
        answer.setAnalysis(createDTO.getAnalysis());
        answerRepository.save(answer);

        return convertToDTO(savedQuestion);
    }

    /**
     * 获取教师的所有题目
     */
    public List<QuestionDTO> getQuestionsByTeacher(String teacherId) {
        List<Question> questions = questionRepository.findByCreatorId(teacherId);
        return questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取题目详情
     */
    public QuestionDTO getQuestionById(Long questionId, String teacherId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        // 验证权限
        if (!question.getCreatorId().equals(teacherId)) {
            throw new RuntimeException("无权访问该题目");
        }

        return convertToDTO(question);
    }

    /**
     * 更新题目
     */
    @Transactional
    public QuestionDTO updateQuestion(Long questionId, QuestionCreateDTO updateDTO, String teacherId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        // 验证权限
        if (!question.getCreatorId().equals(teacherId)) {
            throw new RuntimeException("无权修改该题目");
        }

        // 更新题目基本信息
        question.setContent(updateDTO.getContent());
        question.setQuestionType(updateDTO.getQuestionType());
        Question updatedQuestion = questionRepository.save(question);

        // 删除旧选项并添加新选项
        optionRepository.deleteByQuestionId(questionId);
        if ((updateDTO.getQuestionType() == QuestionType.SINGLE_CHOICE ||
                updateDTO.getQuestionType() == QuestionType.MULTIPLE_CHOICE) &&
                updateDTO.getOptions() != null) {

            for (QuestionCreateDTO.OptionDTO optionDTO : updateDTO.getOptions()) {
                Option option = new Option();
                option.setQuestionId(questionId);
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOptionContent(optionDTO.getOptionContent());
                option.setIsCorrect(optionDTO.getIsCorrect());
                optionRepository.save(option);
            }
        }

        // 处理判断题选项（自动生成"正确"和"错误"两个选项）
        if (updateDTO.getQuestionType() == QuestionType.TRUE_FALSE) {
            String correctAnswer = updateDTO.getCorrectAnswer();
            
            Option correctOption = new Option();
            correctOption.setQuestionId(questionId);
            correctOption.setOptionLabel("A");
            correctOption.setOptionContent("正确");
            correctOption.setIsCorrect("正确".equals(correctAnswer));
            optionRepository.save(correctOption);

            Option wrongOption = new Option();
            wrongOption.setQuestionId(questionId);
            wrongOption.setOptionLabel("B");
            wrongOption.setOptionContent("错误");
            wrongOption.setIsCorrect("错误".equals(correctAnswer));
            optionRepository.save(wrongOption);
        }

        // 更新答案
        Answer answer = answerRepository.findByQuestionId(questionId)
                .orElse(new Answer());
        answer.setQuestionId(questionId);
        answer.setCorrectAnswer(updateDTO.getCorrectAnswer());
        answer.setAnalysis(updateDTO.getAnalysis());
        answerRepository.save(answer);

        return convertToDTO(updatedQuestion);
    }

    /**
     * 删除题目（物理删除）
     */
    @Transactional
    public void deleteQuestion(Long questionId, String teacherId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        // 验证权限
        if (!question.getCreatorId().equals(teacherId)) {
            throw new RuntimeException("无权删除该题目");
        }

        // 删除相关数据
        optionRepository.deleteByQuestionId(questionId);
        answerRepository.deleteByQuestionId(questionId);
        questionRepository.deleteById(questionId);
    }

    /**
     * 按题型获取题目
     */
    public List<QuestionDTO> getQuestionsByType(String teacherId, String questionTypeStr) {
        try {
            QuestionType questionType = QuestionType.valueOf(questionTypeStr.toUpperCase());
            List<Question> questions = questionRepository.findByCreatorIdAndQuestionType(teacherId, questionType);
            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的题型: " + questionTypeStr);
        }
    }

    /**
     * 分页查询题目（物理删除版本）
     */
    public Page<QuestionDTO> getQuestionsByPage(String teacherId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("createdTime").descending());
        Page<Question> questions = questionRepository.findByCreatorId(teacherId, pageable);
        return questions.map(this::convertToDTO);
    }

    /**
     * 按题型分页查询题目（物理删除版本）
     */
    public Page<QuestionDTO> getQuestionsByTypeAndPage(String teacherId, String questionTypeStr, int pageNum, int pageSize) {
        try {
            QuestionType questionType = QuestionType.valueOf(questionTypeStr.toUpperCase());
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("createdTime").descending());

            Page<Question> questions = questionRepository.findByCreatorIdAndQuestionType(teacherId, questionType, pageable);
            return questions.map(this::convertToDTO);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的题型: " + questionTypeStr);
        }
    }

    /**
     * 批量查询题目（安全版本 - 只返回当前教师的题目）
     */
    public List<QuestionDTO> getQuestionsByIds(List<Long> questionIds, String teacherId) {
        List<Question> questions = questionRepository.findByIdInAndCreatorId(questionIds, teacherId);
        return questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 随机抽取指定数量的题目（按题型）
     */
    public List<QuestionDTO> getRandomQuestionsByType(String teacherId, String questionTypeStr, int count) {
        List<Question> questions = questionRepository.findRandomQuestionsByType(
                teacherId, questionTypeStr.toUpperCase());

        return questions.stream()
                .limit(count)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 随机抽取指定数量的题目（任意题型）
     */
    public List<QuestionDTO> getRandomQuestions(String teacherId, int count) {
        List<Question> questions = questionRepository.findRandomQuestions(teacherId);

        return questions.stream()
                .limit(count)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 导入Excel题目 - 改进版（正确处理空行和格式验证）
     */
    @Transactional
    public String importQuestionsFromExcel(MultipartFile file, String teacherId) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int successCount = 0;
            int errorCount = 0;
            int totalRowsProcessed = 0;

            // 跳过标题行，从第二行开始处理
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                // 跳过完全空白的行
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalRowsProcessed++;

                try {
                    // Excel列格式：题型 | 题干 | 选项A | 选项B | 选项C | 选项D | 正确答案 | 解析
                    String questionTypeStr = getCellStringValue(row, 0);
                    String content = getCellStringValue(row, 1);

                    // 验证必要字段
                    if (questionTypeStr == null || questionTypeStr.trim().isEmpty()) {
                        log.warn("第{}行: 题型为空，跳过", i + 1);
                        errorCount++;
                        continue;
                    }

                    if (content == null || content.trim().isEmpty()) {
                        log.warn("第{}行: 题干为空，跳过", i + 1);
                        errorCount++;
                        continue;
                    }

                    // 验证题型是否有效
                    QuestionType questionType;
                    try {
                        questionType = convertToQuestionType(questionTypeStr);
                    } catch (RuntimeException e) {
                        log.warn("第{}行: 无效题型 '{}', 错误: {}", i + 1, questionTypeStr, e.getMessage());
                        errorCount++;
                        continue;
                    }

                    // 创建题目
                    Question question = new Question();
                    question.setQuestionType(questionType);
                    question.setContent(content.trim());
                    question.setCreatorId(teacherId);
                    Question savedQuestion = questionRepository.save(question);

                    // 处理选项和答案
                    String correctAnswer = getCellStringValue(row, 6); // 正确答案在第7列
                    String analysis = getCellStringValue(row, 7);     // 解析在第8列

                    // 对于选择题，验证是否有选项
                    if (isChoiceQuestion(question.getQuestionType())) {
                        if (!hasValidOptions(row)) {
                            log.warn("第{}行: 选择题缺少有效选项", i + 1);
                            // 删除已创建的题目
                            questionRepository.delete(savedQuestion);
                            errorCount++;
                            continue;
                        }
                        saveChoiceOptions(savedQuestion.getId(), row, correctAnswer);
                    }

                    // 对于判断题，自动生成选项
                    if (question.getQuestionType() == QuestionType.TRUE_FALSE) {
                        saveTrueFalseOptions(savedQuestion.getId(), correctAnswer);
                    }

                    // 保存答案和解析（所有题型都需要）
                    saveAnswerWithAnalysis(savedQuestion.getId(), correctAnswer, analysis, question.getQuestionType());

                    successCount++;
                    log.info("第{}行: 题目导入成功 - {}", i + 1, content.substring(0, Math.min(20, content.length())) + "...");

                } catch (Exception e) {
                    errorCount++;
                    log.error("第{}行导入失败: {}", i + 1, e.getMessage(), e);
                }
            }

            String resultMessage = String.format(
                    "Excel题目导入完成！处理%d行数据，成功导入%d题，失败%d题，文件名: %s",
                    totalRowsProcessed, successCount, errorCount, file.getOriginalFilename()
            );

            log.info(resultMessage);
            return resultMessage;

        } catch (Exception e) {
            log.error("Excel文件导入失败: {}", e.getMessage(), e);
            throw new RuntimeException("Excel文件导入失败: " + e.getMessage());
        }
    }

    /**
     * 检查行是否为空（所有单元格都为空）
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        // 检查前几列是否都为空
        for (int i = 0; i < 8; i++) { // 检查前8列
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(row, i);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查选择题是否有有效选项
     */
    private boolean hasValidOptions(Row row) {
        int[] optionColumns = {2, 3, 4, 5}; // 选项A-D对应的列

        for (int colIndex : optionColumns) {
            String optionContent = getCellStringValue(row, colIndex);
            if (optionContent != null && !optionContent.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * 保存选择题选项
     */
    private void saveChoiceOptions(Long questionId, Row row, String correctAnswer) {
        String[] optionLabels = {"A", "B", "C", "D"};
        int[] optionColumns = {2, 3, 4, 5}; // 选项A-D对应的列

        for (int i = 0; i < optionLabels.length; i++) {
            String optionContent = getCellStringValue(row, optionColumns[i]);
            if (optionContent != null && !optionContent.trim().isEmpty()) {
                Option option = new Option();
                option.setQuestionId(questionId);
                option.setOptionLabel(optionLabels[i]);
                option.setOptionContent(optionContent);

                // 处理多选题答案（如"AB"、"ACD"）和单选题答案（如"A"）
                boolean isCorrect = correctAnswer != null &&
                        correctAnswer.toUpperCase().contains(optionLabels[i]);
                option.setIsCorrect(isCorrect);

                optionRepository.save(option);
            }
        }
    }

    /**
     * 保存判断题选项
     */
    private void saveTrueFalseOptions(Long questionId, String correctAnswer) {
        boolean isCorrect = normalizeTrueFalseAnswer(correctAnswer);
        
        Option correctOption = new Option();
        correctOption.setQuestionId(questionId);
        correctOption.setOptionLabel("A");
        correctOption.setOptionContent("正确");
        correctOption.setIsCorrect(isCorrect);
        optionRepository.save(correctOption);

        Option wrongOption = new Option();
        wrongOption.setQuestionId(questionId);
        wrongOption.setOptionLabel("B");
        wrongOption.setOptionContent("错误");
        wrongOption.setIsCorrect(!isCorrect);
        optionRepository.save(wrongOption);
    }
    
    /**
     * 标准化判断题答案
     * 支持：A/B/正确/错误/TRUE/FALSE/对/错
     * 返回 true 表示正确，false 表示错误
     */
    private boolean normalizeTrueFalseAnswer(String answer) {
        if (answer == null) return false;
        
        String upperAnswer = answer.toUpperCase().trim();
        
        if ("A".equals(upperAnswer) || "正确".equals(answer) || "TRUE".equals(upperAnswer) || "对".equals(answer)) {
            return true;
        }
        if ("B".equals(upperAnswer) || "错误".equals(answer) || "FALSE".equals(upperAnswer) || "错".equals(answer)) {
            return false;
        }
        
        return "正确".equals(answer);
    }

    /**
     * 保存带解析的答案
     */
    private void saveAnswerWithAnalysis(Long questionId, String correctAnswer, String analysis, QuestionType questionType) {
        String normalizedAnswer = correctAnswer;
        
        // 只对判断题进行标准化处理
        if (questionType == QuestionType.TRUE_FALSE && correctAnswer != null) {
            boolean isCorrect = normalizeTrueFalseAnswer(correctAnswer);
            normalizedAnswer = isCorrect ? "正确" : "错误";
        }
        
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setCorrectAnswer(normalizedAnswer);
        answer.setAnalysis(analysis != null ? analysis : "");
        answerRepository.save(answer);
    }

    /**
     * 获取单元格字符串值（改进版 - 更好的空值处理）
     */
    private String getCellStringValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                String stringValue = cell.getStringCellValue();
                return stringValue != null ? stringValue.trim() : null;
            case NUMERIC:
                // 处理数字类型，如果是整数则不显示小数点
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            case FORMULA:
                // 尝试获取公式计算结果
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }


    /**
     * 转换字符串为题型枚举
     */
    private QuestionType convertToQuestionType(String typeStr) {
        switch (typeStr.trim()) {
            case "单选题": return QuestionType.SINGLE_CHOICE;
            case "多选题": return QuestionType.MULTIPLE_CHOICE;
            case "判断题": return QuestionType.TRUE_FALSE;
            case "填空题": return QuestionType.FILL_BLANK;
            case "简答题": return QuestionType.SHORT_ANSWER;
            default: throw new RuntimeException("不支持的题型: " + typeStr);
        }
    }

    /**
     * 判断是否为选择题
     */
    private boolean isChoiceQuestion(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE;
    }

    /**
     * 导入JSON题目（占位符）
     */
    @Transactional
    public String importQuestionsFromJson(MultipartFile file, String teacherId) {
        try {
            return "JSON题目导入功能待实现，文件名: " + file.getOriginalFilename();
        } catch (Exception e) {
            throw new RuntimeException("JSON文件导入失败: " + e.getMessage());
        }
    }

    /**
     * 转换为DTO
     */
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setQuestionType(question.getQuestionType());
        dto.setCreatorId(question.getCreatorId());
        dto.setCreatedTime(question.getCreatedTime());
        dto.setUpdatedTime(question.getUpdatedTime());

        // 设置选项
        List<Option> options = optionRepository.findByQuestionId(question.getId());
        List<QuestionDTO.OptionDTO> optionDTOs = options.stream().map(option -> {
            QuestionDTO.OptionDTO optionDTO = new QuestionDTO.OptionDTO();
            optionDTO.setId(option.getId());
            optionDTO.setOptionLabel(option.getOptionLabel());
            optionDTO.setOptionContent(option.getOptionContent());
            optionDTO.setIsCorrect(option.getIsCorrect());
            return optionDTO;
        }).collect(Collectors.toList());
        dto.setOptions(optionDTOs);

        // 设置答案
        Answer answer = answerRepository.findByQuestionId(question.getId()).orElse(null);
        if (answer != null) {
            dto.setCorrectAnswer(answer.getCorrectAnswer());
            dto.setAnalysis(answer.getAnalysis());
        }

        return dto;
    }
}
