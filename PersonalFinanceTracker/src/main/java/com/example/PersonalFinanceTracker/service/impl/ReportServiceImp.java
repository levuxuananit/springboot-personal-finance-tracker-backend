package com.example.PersonalFinanceTracker.service.impl;

import com.example.PersonalFinanceTracker.dto.report.ExportReportRequest;
import com.example.PersonalFinanceTracker.entity.Transaction;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;


import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImp {
    private final TransactionRepository transactionRepository;

    public String exportSummaryPdf(Long userId, ExportReportRequest request) throws Exception {

        BigDecimal income = Optional.ofNullable(
                transactionRepository.getTotalIncome(userId, request.getMonth(), request.getYear())
        ).orElse(BigDecimal.ZERO);

        BigDecimal expense = Optional.ofNullable(
                transactionRepository.getTotalExpense(userId, request.getMonth(), request.getYear())
        ).orElse(BigDecimal.ZERO);

        BigDecimal balance = income.subtract(expense);

        List<Object[]> topExpenses =
                transactionRepository.getTopExpenses(userId,
                        request.getMonth(),
                        request.getYear(),
                        (Pageable) PageRequest.of(0, 3));

        String fileName = "report_" + userId + "_" + request.getMonth() + "_" + request.getYear() + ".pdf";
        String path = "reports/" + fileName;

        generatePdf(path, income, expense, balance, topExpenses);

        return path;

    }

    private void generatePdf(String path,
                             BigDecimal income,
                             BigDecimal expense,
                             BigDecimal balance,
                             List<Object[]> topExpenses) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));

        document.open();

        document.add(new Paragraph("MONTHLY FINANCIAL SUMMARY"));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Income: $" + income));
        document.add(new Paragraph("Expense: $" + expense));
        document.add(new Paragraph("Balance: $" + balance));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Top 3 Expenses"));

        PdfPTable table = new PdfPTable(2);

        table.addCell("Category");
        table.addCell("Amount");

        for (Object[] row : topExpenses) {

            table.addCell(row[0].toString());
            table.addCell(row[1].toString());
        }

        document.add(table);

        document.close();
    }

    public byte[] generateMonthlyReport(Long userId) {

        List<Transaction> transactions =
                transactionRepository.findByUserId(userId);
        return new byte[0];
    }

}
