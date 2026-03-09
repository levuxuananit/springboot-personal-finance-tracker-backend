package com.example.PersonalFinanceTracker.security;

<<<<<<< HEAD
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
=======
import org.springframework.http.MediaType;
>>>>>>> feature/budget-list
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
import java.io.IOException;
import java.util.Map;
=======
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
>>>>>>> feature/budget-list

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
<<<<<<< HEAD
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
=======
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
}

>>>>>>> feature/budget-list
