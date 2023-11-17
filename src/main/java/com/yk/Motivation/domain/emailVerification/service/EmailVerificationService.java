package com.yk.Motivation.domain.emailVerification.service;

import com.yk.Motivation.base.app.AppConfig;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.attr.service.AttrService;
import com.yk.Motivation.domain.email.service.EmailService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {
    private final EmailService emailService;
    private final AttrService attrService;
    @Autowired
    @Lazy
    private MemberService memberService;

    // 조회

    // 명령
    @Transactional
    public CompletableFuture<RsData> send(Member member) {
        String subject = "[%s 이메일인증] 안녕하세요 %s님. 링크를 클릭하여 회원가입을 완료해주세요."
                .formatted(
                        AppConfig.getSiteName(),
                        member.getUsername()
                );
        String body = genEmailVerificationUrl(member);

        return emailService.sendAsync(member.getEmail(), subject, body);
    }

    private String genEmailVerificationUrl(Member member) {
        return genEmailVerificationUrl(member.getId());
    }

    private String genEmailVerificationUrl(long memberId) {
        String code = genEmailVerificationCode(memberId);
        String verificationUrl = AppConfig.getSiteBaseUrl() + "/emailVerification/verify?memberId=%d&code=%s".formatted(memberId, code);

        // HTML 이메일 본문을 생성합니다.
        String htmlEmailBody = String.format(
                "<html>" +
                        "    <body>" +
                        "        <p>이메일 주소를 확인하려면 다음 링크를 클릭하세요: " +
                        "            <a href='%s'>이메일 인증하기</a>" +
                        "        </p>" +
                        "    </body>" +
                        "</html>",
                verificationUrl);

        return htmlEmailBody;
    }

    private String genEmailVerificationCode(long memberId) {
        String code = UUID.randomUUID().toString();
        attrService.set("member__%d__extra__emailVerificationCode".formatted(memberId), code, LocalDateTime.now().plusSeconds(60 * 60));

        return code;
    }

    @Transactional
    public RsData verify(long memberId, String code) {
        RsData checkVerificationCodeValidRs = checkVerificationCodeValid(memberId, code);

        if (!checkVerificationCodeValidRs.isSuccess()) return checkVerificationCodeValidRs;

        setEmailVerified(memberId);

        return RsData.of("S-1", "이메일인증이 완료되었습니다.");
    }

    private RsData checkVerificationCodeValid(long memberId, String code) {
        String foundCode = attrService.get("member__%d__extra__emailVerificationCode".formatted(memberId), "");

        if (!foundCode.equals(code)) return RsData.of("F-1", "만료 되었거나 유효하지 않은 코드입니다.");

        return RsData.of("S-1", "인증된 코드 입니다.");
    }

    private void setEmailVerified(long memberId) {
        memberService.setEmailVerified(memberId);
    }
}