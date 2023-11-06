package com.example.demo.domain.member.controller;

import com.example.demo.base.rq.Rq;
import com.example.demo.base.rsData.RsData;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.exception.EmailNotVerifiedAccessDeniedException;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.standard.util.Ut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/templates/usr/member")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String showLogin() {
        return "templates/usr/member/login";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/join")
    public String showJoin() {
        return "templates/usr/member/join";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/join")
    public String join(@Valid JoinForm joinForm) {
        RsData<Member> joinRs = memberService.join(
                joinForm.getUsername(),
                joinForm.getPassword(),
                joinForm.getNickname(),
                joinForm.getEmail(),
                joinForm.getProfileImg()
        );

        return rq.redirectOrBack("/", joinRs);
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/checkUsernameDup")
    @ResponseBody
    public RsData<String> checkUsernameDup(
            @NotBlank @Length(min = 4) String username
    ) {
        return memberService.checkUsernameDup(username);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkProducerNameDup")
    @ResponseBody
    public RsData<String> checkProducerNameDup(
            @NotBlank @Length(min = 2) String producerName
    ) {
        return memberService.checkProducerNameDup(rq.getMember(), producerName);
    }

    public boolean assertCheckPasswordAuthCodeVerified() {
        memberService
                .checkCheckPasswordAuthCode(rq.getMember(), rq.getParam("checkPasswordAuthCode", ""))
                .optional()
                .filter(RsData::isFail)
                .ifPresent((rsData) -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/checkEmailDup")
    @ResponseBody
    public RsData<String> checkEmailDup(
            @NotBlank @Length(min = 4) String email
    ) {
        return memberService.checkEmailDup(email);
    }

    public boolean assertCurrentMemberVerified() {
        if (!memberService.isEmailVerified(rq.getMember()))
            throw new EmailNotVerifiedAccessDeniedException("이메일 인증 후 이용해주세요.");

        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/notVerified")
    public String showNotVerified() {
        return "templates/usr/member/notVerified";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/findUsername")
    public String showFindUsername() {
        return "templates/usr/member/findUsername";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/findUsername")
    public String findUsername(
            @NotBlank @Length(min = 4) String email
    ) {
        return memberService.findByEmail(email)
                .map(member ->
                        rq.redirect(
                                "/templates/usr/member/login?lastUsername=%s".formatted(member.getUsername()),
                                "해당 회원의 아이디는 `%s` 입니다.".formatted(member.getUsername())
                        )
                )
                .orElseGet(() -> rq.historyBack("`%s` (은)는 존재하지 않은 회원 이메일 입니다.".formatted(email)));
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/findPassword")
    public String showFindPassword() {
        return "templates/usr/member/findPassword";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/findPassword")
    public String findPassword(
            @NotBlank @Length(min = 4) String username,
            @NotBlank @Length(min = 4) String email
    ) {
        return memberService.findByUsernameAndEmail(username, email)
                .map(member -> {
                    memberService.sendTempPasswordToEmail(member);
                    return rq.redirect(
                            "/templates/usr/member/login?lastUsername=%s".formatted(member.getUsername()),
                            "해당 회원의 이메일로 임시 비밀번호를 발송하였습니다."
                    );
                }).orElseGet(() -> rq.historyBack("일치하는 회원이 존재하지 않습니다."));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public String showMe() {
        return "templates/usr/member/me";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify")
    public String showModify() {
        return "templates/usr/member/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String modify(@Valid ModifyForm modifyForm) {

        RsData<Member> modifyRs = memberService.modify(
                rq.getMember().getId(),
                modifyForm.getPassword(),
                modifyForm.getNickname(),
                modifyForm.getProfileImg()
        );

        return rq.redirectOrBack("/templates/usr/member/me", modifyRs);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkPassword")
    public String showCheckPassword() {
        return "templates/usr/member/checkPassword";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkPassword")
    public String checkPassword(
            @NotBlank @Length(min = 4) String password,
            @NotBlank String redirectUrl
    ) {        if (!memberService.isSamePassword(rq.getMember(), password))
            return rq.historyBack("비밀번호가 일치하지 않습니다.");

        String code = memberService.genCheckPasswordAuthCode(rq.getMember());

        redirectUrl = Ut.url.modifyQueryParam(redirectUrl, "checkPasswordAuthCode", code);

        return rq.redirect(redirectUrl);
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class JoinForm {
        @NotBlank
        @Length(min = 4)
        private String username;
        @NotBlank
        @Length(min = 4)
        private String nickname;
        @NotBlank
        @Length(min = 4)
        private String password;
        @NotBlank
        @Length(min = 4)
        private String email;
        private MultipartFile profileImg;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class ModifyForm {
        @NotBlank
        @Length(min = 4)
        private String nickname;
        @Length(min = 4)
        private String password;
        private MultipartFile profileImg;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/beProducer")
    public String showBeProducer() {
        return "templates/usr/member/beProducer";
    }

    @SneakyThrows
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/beProducer")
    public String beProducer(@NotBlank @Length(min = 2) String producerName) {
        Member member = rq.getMember();

        RsData<Member> rs = memberService.beProducer(member.getId(), producerName);

        return rq.redirectOrBack("/templates/usr/member/me", rs);
    }

}