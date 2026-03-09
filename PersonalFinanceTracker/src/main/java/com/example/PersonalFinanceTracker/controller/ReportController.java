package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.ReportCategoryDTO;
import com.example.PersonalFinanceTracker.dto.ReportMonthlyDTO;
import com.example.PersonalFinanceTracker.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/category")
    public ResponseEntity<?> getCategoryReport(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        ReportCategoryDTO data = reportService.getCategoryReport(type, month, year);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Category breakdown fetched successfully");
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        ReportMonthlyDTO data = reportService.getMonthlyReport(year, month);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Monthly financial report fetched successfully");
        body.put("data", data);
        return ResponseEntity.ok(body);
    }
}
