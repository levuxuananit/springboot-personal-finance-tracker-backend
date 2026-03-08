package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.report.ExportReportRequest;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import com.example.PersonalFinanceTracker.service.impl.ReportServiceImp;
//import jdk.internal.net.http.common.ImmutableExtendedSSLSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceImp reportService;
    private final UserRepository userRepository;

    @PostMapping("/export/pdf")
    public ResponseEntity<Map<String, Object>> exportPdf(
            @RequestBody ExportReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws Exception {

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String filePath = reportService.exportSummaryPdf(user.getId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Summary report PDF generated successfully");
        response.put("downloadUrl", filePath);

        return ResponseEntity.ok(response);
    }
}