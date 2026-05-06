package com.example.exam_system.examscore.service;

import com.example.exam_system.exammanage.entity.Exam;
import com.example.exam_system.exammanage.repository.ExamRepository;
import com.example.exam_system.examscore.entity.ExamScore;
import com.example.exam_system.examscore.repository.ExamScoreRepository;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    @Autowired
    private ExamScoreRepository examScoreRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserRepository userRepository;

    public byte[] exportExamScores(Long examId) throws IOException {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("考试不存在"));

        // 从 ExamScore 表获取成绩数据（不受学生删除 ExamRecord 影响）
        List<ExamScore> scores = examScoreRepository.findByExamId(examId);

        List<String> studentIds = scores.stream()
                .map(ExamScore::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, User> studentMap = studentIds.stream()
                .map(id -> userRepository.findByUsername(id))
                .filter(opt -> opt.isPresent())
                .collect(Collectors.toMap(
                        opt -> opt.get().getUsername(),
                        opt -> opt.get()
                ));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("成绩单");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle scoreStyle = createScoreStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"学工号", "姓名", "客观题分数", "主观题分数", "总分", "参考账号"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            int rowNum = 1;
            for (ExamScore score : scores) {
                Row row = sheet.createRow(rowNum++);

                User student = studentMap.get(score.getStudentId());

                // 学工号
                Cell staffIdCell = row.createCell(0);
                staffIdCell.setCellValue(student != null && student.getStudentStaffId() != null ? student.getStudentStaffId() : "-");
                staffIdCell.setCellStyle(dataStyle);

                // 姓名
                Cell nameCell = row.createCell(1);
                String studentName = student != null ? (student.getNickname() != null ? student.getNickname() : student.getUsername()) : score.getStudentId();
                nameCell.setCellValue(studentName);
                nameCell.setCellStyle(dataStyle);

                // 客观题分数
                Cell objectiveCell = row.createCell(2);
                Double objectiveScore = score.getObjectiveScore();
                objectiveCell.setCellValue(objectiveScore != null ? objectiveScore : 0);
                objectiveCell.setCellStyle(scoreStyle);

                // 主观题分数
                Cell subjectiveCell = row.createCell(3);
                Double subjectiveScore = score.getSubjectiveScore();
                subjectiveCell.setCellValue(subjectiveScore != null ? subjectiveScore : 0);
                subjectiveCell.setCellStyle(scoreStyle);

                // 总分
                Cell totalCell = row.createCell(4);
                Double totalScore = score.getTotalScore();
                totalCell.setCellValue(totalScore != null ? totalScore : 0);
                totalCell.setCellStyle(scoreStyle);

                // 参考账号
                Cell accountCell = row.createCell(5);
                accountCell.setCellValue(score.getStudentId());
                accountCell.setCellStyle(dataStyle);
            }

            if (!scores.isEmpty()) {
                Row statsRow = sheet.createRow(rowNum + 1);
                CellStyle statsStyle = workbook.createCellStyle();
                Font statsFont = workbook.createFont();
                statsFont.setBold(true);
                statsStyle.setFont(statsFont);

                Cell statsLabel = statsRow.createCell(0);
                statsLabel.setCellValue("统计");
                statsLabel.setCellStyle(statsStyle);

                Cell countCell = statsRow.createCell(1);
                countCell.setCellValue("参考人数: " + scores.size());
                countCell.setCellStyle(statsStyle);

                Cell emptyCell = statsRow.createCell(2);
                emptyCell.setCellValue("");

                Cell emptyCell2 = statsRow.createCell(3);
                emptyCell2.setCellValue("");

                Cell emptyCell3 = statsRow.createCell(4);
                emptyCell3.setCellValue("");

                double avgScore = scores.stream()
                        .mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0)
                        .average()
                        .orElse(0);
                Cell avgCell = statsRow.createCell(5);
                avgCell.setCellValue("平均分: " + String.format("%.2f", avgScore));
                avgCell.setCellStyle(statsStyle);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createScoreStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));

        return style;
    }

    public String getExportFileName(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        String examName = exam.getExamName();
        return examName.replaceAll("[\\\\/:*?\"<>|]", "_") + "_成绩单.xlsx";
    }
}
