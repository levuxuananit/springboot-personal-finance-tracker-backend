package com.example.PersonalFinanceTracker.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import org.springframework.http.MediaType;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> body = Map.of(
                "success", false,
                "message", "Unauthorized – Please login to access this resource"
        );

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}

public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    response.getWriter().write("""
                {"success":false,"message":"Unauthorized – Please login to access notification settings"}
                """.trim());
}



