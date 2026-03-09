package com.example.PersonalFinanceTracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Tạo body phản hồi JSON
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", "Unauthorized");

        // Gộp thông báo từ cả hai nhánh để rõ ràng nhất
        body.put("message", "Unauthorized – Please login to access this resource or notification settings");

        // Có thể thêm chi tiết lỗi nếu cần debug
        body.put("path", request.getServletPath());

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}