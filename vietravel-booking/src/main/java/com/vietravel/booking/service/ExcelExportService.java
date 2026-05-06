package com.vietravel.booking.service;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.Payment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ByteArrayInputStream exportBookings(List<Booking> bookings) throws IOException {
        String[] columns = {"Mã Booking", "Khách hàng", "Tour", "Ngày đặt", "Tổng tiền", "Trạng thái"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings");

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Row for header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Booking b : bookings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getBookingCode());
                row.createCell(1).setCellValue(b.getContactName());
                row.createCell(2).setCellValue(b.getDeparture() != null && b.getDeparture().getTour() != null ? b.getDeparture().getTour().getTitle() : "N/A");
                row.createCell(3).setCellValue(b.getCreatedAt() != null ? b.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
                row.createCell(4).setCellValue(b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0);
                row.createCell(5).setCellValue(b.getStatus() != null ? b.getStatus().name() : "PENDING");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportPayments(List<Payment> payments) throws IOException {
        String[] columns = {"Mã GD (TxnRef)", "Mã Booking", "Số tiền", "Phương thức", "Ngày thanh toán", "Trạng thái"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payments");

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Payment p : payments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getTxnRef());
                row.createCell(1).setCellValue(p.getBooking() != null ? p.getBooking().getBookingCode() : "N/A");
                row.createCell(2).setCellValue(p.getAmount() != null ? p.getAmount().doubleValue() : 0.0);
                row.createCell(3).setCellValue(p.getMethod() != null ? p.getMethod().name() : "N/A");
                row.createCell(4).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(formatter) : "N/A");
                row.createCell(5).setCellValue(p.getStatus() != null ? p.getStatus().name() : "N/A");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
