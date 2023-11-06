package com.example.demo.base.security;

import com.example.demo.domain.member.exception.EmailNotVerifiedAccessDeniedException;
import com.example.demo.standard.util.Ut;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import java.io.IOException;

public class CustomAccessDeniedHandler extends AccessDeniedHandlerImpl {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (accessDeniedException instanceof EmailNotVerifiedAccessDeniedException) {
            response.sendRedirect("/templates/usr/member/notVerified?msg=" + Ut.url.encodeWithTtl(accessDeniedException.getMessage()));
            return;
        }

        super.handle(request, response, accessDeniedException);
    }
}