package com.yk.Motivation.domain.member.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.exception.EmailNotVerifiedAccessDeniedException;
import com.yk.Motivation.domain.member.service.MemberService;
import com.yk.Motivation.domain.order.entity.Order;
import com.yk.Motivation.domain.order.service.OrderService;
import com.yk.Motivation.standard.util.Ut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/usr/member")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final MemberService memberService;
    private final OrderService orderService;
    private final LectureService lectureService;
    private final Rq rq;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String showLogin() {
        return "usr/member/login";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
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
        return "usr/member/notVerified";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/findUsername")
    public String showFindUsername() {
        return "usr/member/findUsername";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/findUsername")
    public String findUsername(
            @NotBlank @Length(min = 4) String email
    ) {
        return memberService.findByEmail(email)
                .map(member ->
                        rq.redirect(
                                "/usr/member/login?lastUsername=%s".formatted(member.getUsername()),
                                "해당 회원의 아이디는 `%s` 입니다.".formatted(member.getUsername())
                        )
                )
                .orElseGet(() -> rq.historyBack("`%s` (은)는 존재하지 않은 회원 이메일 입니다.".formatted(email)));
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/findPassword")
    public String showFindPassword() {
        return "usr/member/findPassword";
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
                            "/usr/member/login?lastUsername=%s".formatted(member.getUsername()),
                            "해당 회원의 이메일로 임시 비밀번호를 발송하였습니다."
                    );
                }).orElseGet(() -> rq.historyBack("일치하는 회원이 존재하지 않습니다."));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public String showMe() {
        return "usr/member/me";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify")
    public String showModify() {
        return "usr/member/modify";
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

        return rq.redirectOrBack("/usr/member/me", modifyRs);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkPassword")
    public String showCheckPassword() {
        return "usr/member/checkPassword";
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
        return "usr/member/beProducer";
    }

    @SneakyThrows
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/beProducer")
    public String beProducer(@NotBlank @Length(min = 2) String producerName) {
        Member member = rq.getMember();

        RsData<Member> rs = memberService.beProducer(member.getId(), producerName);

        return rq.redirectOrBack("/usr/member/me", rs);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myPayments")
    public String showPaymentsList(Model model) {

        List<Order> orders = orderService.findByBuyerIdAndIsPaidTrue(rq.getMember().getId());

        model.addAttribute("orders", orders);

        return "usr/member/myPayments";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myLectures")
    public String showLectureList(Model model) {

        Member member = memberService.findById(rq.getMember().getId()).get();

        model.addAttribute("lectures", member.getLectures());

        return "usr/member/myLectures";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/enroll/{id}")
    public String showLectureListAfterEnroll(
            @PathVariable Long id
    ) {

        Member member = memberService.findById(rq.getMember().getId()).get();

        memberService.addFreeLecture(id);

        return rq.redirectOrBack("/usr/member/myLectures", RsData.of("S-1", "%d번 강의가 추가되었습니다.".formatted(id)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myEnrolledLectures")
    public String showEnrolledLectureList(Model model) {

        List<Lecture> myEnrolledLectures = lectureService.findAllByProducerId(rq.getMember().getId());

        model.addAttribute("lectures", myEnrolledLectures);

        return "usr/member/myEnrolledLectures";
    }
}