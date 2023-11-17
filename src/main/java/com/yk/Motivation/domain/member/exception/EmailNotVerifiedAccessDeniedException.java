package com.yk.Motivation.domain.member.exception;

import org.springframework.security.access.AccessDeniedException;

public class EmailNotVerifiedAccessDeniedException extends AccessDeniedException {
    public EmailNotVerifiedAccessDeniedException(String msg) {
        super(msg);
    }
}