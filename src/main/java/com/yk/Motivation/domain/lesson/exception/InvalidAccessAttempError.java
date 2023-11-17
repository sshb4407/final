package com.yk.Motivation.domain.lesson.exception;

import org.springframework.security.access.AccessDeniedException;

public class InvalidAccessAttempError extends AccessDeniedException {
    public InvalidAccessAttempError(String msg) {
        super(msg);
    }
}
