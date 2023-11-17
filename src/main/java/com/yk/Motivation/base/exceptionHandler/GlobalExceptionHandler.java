package com.yk.Motivation.base.exceptionHandler;

import com.yk.Motivation.base.exception.NeedHistoryBackException;
import com.yk.Motivation.base.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice // 모든 Controller 나 RestController 에서 발생한 예외를 잡아 처리해!
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final Rq rq;

    @ExceptionHandler(NeedHistoryBackException.class) // NeedHistoryBackException 을 Handle 하겠다!
    @ResponseStatus(HttpStatus.FORBIDDEN) // 요 예외가 터지면 응답 할 HTTP 상태 코드는 'FORBIDDEN' (403)
    public String handle(NeedHistoryBackException ex) {
        return rq.historyBack(ex.getMessage());
    }
}