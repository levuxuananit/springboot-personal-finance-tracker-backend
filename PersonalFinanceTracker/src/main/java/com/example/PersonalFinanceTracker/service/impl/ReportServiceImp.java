package com.example.PersonalFinanceTracker.service.impl;

import com.example.PersonalFinanceTracker.dto.report.ExportReportRequest;
import com.example.PersonalFinanceTracker.entity.Transaction;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImp {

    private final TransactionRepository transactionRepository;

    public String exportSummaryPdf(Long userId, ExportReportRequest request) throws Exception {

        Double income = Optional.ofNullable(
                transactionRepository.getTotalIncome(userId, request.getMonth(), request.getYear())
        ).orElse(0.0);

        Double expense = Optional.ofNullable(
                transactionRepository.getTotalExpense(userId, request.getMonth(), request.getYear())
        ).orElse(0.0);

        double balance = income - expense;

        // ===== Previous month =====
        int prevMonth = request.getMonth() == 1 ? 12 : request.getMonth() - 1;
        int prevYear = request.getMonth() == 1 ? request.getYear() - 1 : request.getYear();

        Double prevIncome = Optional.ofNullable(
                transactionRepository.getTotalIncome(userId, prevMonth, prevYear)
        ).orElse(0.0);

        Double prevExpense = Optional.ofNullable(
                transactionRepository.getTotalExpense(userId, prevMonth, prevYear)
        ).orElse(0.0);

        double prevBalance = prevIncome - prevExpense;

        List<Object[]> topExpenses =
                transactionRepository.getTopExpenses(
                        userId,
                        request.getMonth(),
                        request.getYear(),
                        PageRequest.of(0, 3)
                );

        String fileName = "report_" + userId + "_" + request.getMonth() + "_" + request.getYear() + ".pdf";

        generatePdf(
                fileName,
                income,
                expense,
                balance,
                prevIncome,
                prevExpense,
                prevBalance,
                topExpenses,
                request
        );

        return "reports/" + fileName;
    }


    private void generatePdf(String fileName,
                             double income,
                             double expense,
                             double balance,
                             double prevIncome,
                             double prevExpense,
                             double prevBalance,
                             List<Object[]> topExpenses,
                             ExportReportRequest request) throws Exception {

        String folderPath = "reports";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String path = folderPath + "/" + fileName;

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));

        document.open();

        document.add(new Paragraph("MONTHLY FINANCIAL SUMMARY"));
        document.add(new Paragraph(" "));

        // ===== A. SUMMARY =====
        document.add(new Paragraph("A. Monthly Summary"));

        PdfPTable summaryTable = new PdfPTable(2);

        summaryTable.addCell("Item");
        summaryTable.addCell("Value");

        summaryTable.addCell("Income");
        summaryTable.addCell(String.format("$%,.0f",income));

        summaryTable.addCell("Expenses");
        summaryTable.addCell(String.format("$%,.0f",expense));

        summaryTable.addCell("Balance");
        summaryTable.addCell(String.format("$%,.0f",balance));

        summaryTable.addCell("Status");
        summaryTable.addCell(balance >= 0 ? "Surplus" : "Deficit");

        document.add(summaryTable);

        document.add(new Paragraph(" "));

        // ===== B. TOP EXPENSES =====
        if (expense > 1) {

            document.add(new Paragraph("B. Top 3 Expenses"));

            PdfPTable table = new PdfPTable(3);

            table.addCell("Category");
            table.addCell("Amount");
            table.addCell("Percentage");

            for (Object[] row : topExpenses) {

                double amount = Double.parseDouble(row[1].toString());
                double percent = expense == 0 ? 0 : (amount / expense) * 100;

                table.addCell(row[0].toString());
                table.addCell("$" + amount);
                table.addCell(String.format("%.1f%%", percent));
            }

            document.add(table);
        }

        document.add(new Paragraph(" "));

        document.close();
    }

}