package com.yk.Motivation.domain.emailVerification.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.emailVerification.service.EmailVerificationService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/emailVerification")
@Validated
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;
    private final Rq rq;

    @GetMapping("/verify")
    public String verify(
            long memberId,
            @NotBlank String code
    ) {
        RsData verifyEmailRs = emailVerificationService.verify(memberId, code);

        // 각 상황별 이동해야 하는 URL
        String afterFailUrl = "/";
        String afterSuccessButLogoutUrl = "/usr/member/login";
        String afterSuccessUrl = "/";

        // 인증실패한 상황
        if (verifyEmailRs.isFail()) return rq.redirect(afterFailUrl, verifyEmailRs);

        // 인증성공했지만 로그아웃인 상황
        if (rq.isLogout()) return rq.redirect(afterSuccessButLogoutUrl, verifyEmailRs);

        // 인증성공했고 로그인인 상황
        return rq.redirect(afterSuccessUrl, verifyEmailRs);

    }
}