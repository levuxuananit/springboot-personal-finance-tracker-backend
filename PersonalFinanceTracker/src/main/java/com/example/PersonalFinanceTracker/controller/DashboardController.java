package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.DashboardRequestDTO;
import com.example.PersonalFinanceTracker.dto.DashboardResponseDTO;
import com.example.PersonalFinanceTracker.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<?> getDashboard(@Valid DashboardRequestDTO request) {
        DashboardResponseDTO data = dashboardService.getDashboard(
                request.getMonth(),
                request.getYear()
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Dashboard data fetched successfully");
        body.put("data", data);
        return ResponseEntity.ok(body);
    }
}