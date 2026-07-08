package com.nv.task1.service;

import com.nv.task1.entity.Attendance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AttendanceReportService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ==================== EXCEL ====================

    public byte[] exportToExcel(List<Attendance> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Employee", "Department", "Date", "Punch In", "Punch Out", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Attendance a : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.getEmployee() != null ? a.getEmployee().getName() : "-");
                row.createCell(1).setCellValue(a.getEmployee() != null && a.getEmployee().getDepartment() != null ? a.getEmployee().getDepartment() : "-");
                row.createCell(2).setCellValue(a.getDate() != null ? a.getDate().toString() : "-");
                row.createCell(3).setCellValue(formatTime(a.getPunchIn()));
                row.createCell(4).setCellValue(formatTime(a.getPunchOut()));
                row.createCell(5).setCellValue(a.getStatus() != null ? a.getStatus().name() : "-");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ==================== PDF ====================

    private static final float MARGIN = 40;
    private static final float ROW_HEIGHT = 18;
    // Employee, Department, Date, In, Out, Status
    private static final float[] COL_WIDTHS = {130, 90, 75, 60, 60, 100};
    private static final String[] HEADERS = {"Employee", "Department", "Date", "In", "Out", "Status"};

    public byte[] exportToPdf(List<Attendance> rows) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            float pageHeight = page.getMediaBox().getHeight();
            float y = pageHeight - MARGIN;

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            writeText(cs, bold, 16, MARGIN, y, "Attendance Report");
            y -= 20;
            writeText(cs, regular, 9, MARGIN, y, "Generated: " + LocalDate.now());
            y -= 25;

            y = drawTableHeader(cs, bold, MARGIN, y);

            for (Attendance a : rows) {
                if (y < MARGIN + ROW_HEIGHT) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    y = pageHeight - MARGIN;
                    cs = new PDPageContentStream(doc, page);
                    y = drawTableHeader(cs, bold, MARGIN, y);
                }

                String[] values = {
                        a.getEmployee() != null ? a.getEmployee().getName() : "-",
                        a.getEmployee() != null && a.getEmployee().getDepartment() != null ? a.getEmployee().getDepartment() : "-",
                        a.getDate() != null ? a.getDate().toString() : "-",
                        formatTime(a.getPunchIn()),
                        formatTime(a.getPunchOut()),
                        a.getStatus() != null ? a.getStatus().name() : "-"
                };
                drawRow(cs, regular, MARGIN, y, values);
                y -= ROW_HEIGHT;
            }

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private float drawTableHeader(PDPageContentStream cs, PDFont bold, float x, float y) throws IOException {
        float cx = x;
        for (int i = 0; i < HEADERS.length; i++) {
            writeText(cs, bold, 9, cx, y, HEADERS[i]);
            cx += COL_WIDTHS[i];
        }
        y -= 5;
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y);
        cs.lineTo(x + sum(COL_WIDTHS), y);
        cs.stroke();
        return y - ROW_HEIGHT + 5;
    }

    private void drawRow(PDPageContentStream cs, PDFont font, float x, float y, String[] values) throws IOException {
        float cx = x;
        for (int i = 0; i < values.length; i++) {
            writeText(cs, font, 8.5f, cx, y, truncate(values[i], COL_WIDTHS[i]));
            cx += COL_WIDTHS[i];
        }
    }

    private void writeText(PDPageContentStream cs, PDFont font, float size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "-" : text);
        cs.endText();
    }

    // Rough character-count cap per column so long names don't spill into the next column
    private String truncate(String text, float colWidth) {
        if (text == null) return "-";
        int maxChars = (int) (colWidth / 4.7);
        return text.length() > maxChars ? text.substring(0, Math.max(0, maxChars - 1)) + "." : text;
    }

    private float sum(float[] arr) {
        float s = 0;
        for (float f : arr) s += f;
        return s;
    }

    private String formatTime(LocalTime t) {
        return t != null ? t.format(TIME_FMT) : "-";
    }
}
