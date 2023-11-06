package com.example.demo.base.rq;

import com.example.demo.base.rsData.RsData;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.standard.util.Ut;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope // 빈의 생명주기가 HTTP 요청의 생명주기와 동일하게
public class Rq {
    private final MemberService memberService;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final HttpSession session;
    private Member member = null;
    private final User user; // principal

    public Rq(MemberService memberService, HttpServletRequest req, HttpServletResponse resp, HttpSession session) {
        this.memberService = memberService;
        this.req = req;
        this.resp = resp;
        this.session = session;

        // 현재 로그인한 회원의 인증정보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof User) {
            this.user = (User) authentication.getPrincipal();
        } else {
            this.user = null;
        }
    }

    private String getLoginedMemberUsername() { // 로그인 했다면, 로그인 한 member 의 logindID
        if (isLogout()) return null;

        return user.getUsername();
    }

    public boolean isLogin() {
        return user != null;
    }

    public boolean isLogout() {
        return !isLogin();
    }

    public Member getMember() {
        if (isLogout()) {
            return null;
        }

        if (member == null) {
            member = memberService.findByUsername(getLoginedMemberUsername()).get();
        }

        return member;
    }

    public boolean isAdmin() { // isAdmin 이면 true
        if (isLogout()) return false;

        return getMember().isAdmin();
    }

    public boolean isProducer() { // isProducer 면 true
        if (isLogout()) return false;

        return getMember().isProducer();
    }

    public String getProducerPageName() { // 크리에이터 정보와 신청을 동적으로 보여주려고
        if (isProducer()) return "크리에이터 정보";
        return "크리에이터 신청";
    }

    // 세션 관련 함수
    public void setSession(String name, Object value) {
        session.setAttribute(name, value);
    }

    private Object getSession(String name, Object defaultValue) {
        Object value = session.getAttribute(name);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    private long getSessionAsLong(String name, long defaultValue) {
        Object value = getSession(name, null);

        if (value == null) return defaultValue;

        return (long) value;
    }

    public void removeSession(String name) {
        session.removeAttribute(name);
    }

    // 쿠키 관련
    public void setCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }

    private String getCookie(String name, String defaultValue) {
        Cookie[] cookies = req.getCookies();

        if (cookies == null) {
            return defaultValue;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        return defaultValue;
    }

    private long getCookieAsLong(String name, int defaultValue) {
        String value = getCookie(name, null);

        if (value == null) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }

    public void removeCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }


    public String getAllCookieValuesAsString() {
        StringBuilder sb = new StringBuilder();

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                sb.append(cookie.getName()).append(": ").append(cookie.getValue()).append("\n");
            }
        }

        return sb.toString();
    }

    public String getAllSessionValuesAsString() {
        StringBuilder sb = new StringBuilder();

        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            sb.append(attributeName).append(": ").append(session.getAttribute(attributeName)).append("\n");
        }

        return sb.toString();
    }

    //


    public String historyBack(RsData rs) {
        return historyBack(rs.getMsg());
    }

    public String historyBack(String msg) { // historyBack 시에 toastr 를 위해 내용을 발라서 historyback 시킴

        String referer = req.getHeader("referer"); // referer 정보 가져옴
        String key = "historyBackFailMsg___" + referer; // value 제단
        req.setAttribute("localStorageKeyAboutHistoryBackFailMsg", key);
        req.setAttribute("historyBackFailMsg", Ut.url.withTtl(msg)); // timeout 을 위한 ttl 을 발라서 msg 저장

        // 200 이 아니라 400 으로 응답코드가 지정되도록
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        return "templates/common/js"; //  history back
    }

    public String redirect(String url) {
        return redirect(url, "");
    }

    public String redirect(String url, RsData rs) {
        return redirect(url, rs.getMsg());
    }

    public String redirect(String url, String msg) { // msg 가 있다면, url 에 parameter 로 ?msg= 형태로 redirect
        if (Ut.str.isBlank(msg)) return "redirect:" + url;

        return "redirect:" + Ut.url.modifyQueryParam(url, "msg", Ut.url.encodeWithTtl(msg));
    }

    public String redirectOrBack(String url, RsData rs) { // rsData 가 isSuccess 면 redirect, isFail 이면 history back
        if ( rs.isFail() ) return historyBack(rs);

        return redirect(url, rs);
    }

    public String getProfileImgUrl() { // profileImg 경로 가져오기
        return memberService.getProfileImgUrl(getMember());
    }

    public String getRefererUrl(String defaultValue) { // referer 정보 가져오기
        String referer = req.getHeader("referer");

        if (Ut.str.isBlank(referer)) return defaultValue;

        return referer;
    }

    // url 예시 : https://www.example.com/products/shoes/sneakers
    // 예시에서 Path 는 /products/shoes/sneakers

    public String getRefererUrlPath(String defaultValue) {
        return Ut.url.getPath(getRefererUrl(defaultValue), defaultValue);
    }

    public String getCurrentUrlPath() {
        return Ut.url.getPath(getCurrentUrl(), "");
    }

    // getRequestURL() 은 parameter (queryString) 은 버리고 url 만 return 함
    // 때문에 getQueryString() 으로 parameter 부분을 가져와서 다시 붙여주는 것
    private String getCurrentUrl() {
        String queryString = req.getQueryString();
        return req.getRequestURI() + (Ut.str.hasLength(queryString) ? "?" + queryString : "");
    }

    public String getEncodedCurrentUrl() {
        return Ut.url.encode(getCurrentUrl());
    }

    public String getParam(String paramName, String defaultValue) {
        String value = req.getParameter(paramName);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public String getPathVariable(int index) {
        return getCurrentUrlPath().split("/")[index + 1];
    }

    public long getPathVariableAsLong(int index) {
        return Long.parseLong(getPathVariable(index));
    }

    public String getSuitableListByTagPageBaseUrlByCurrentUrl(String domainName) {
        String currentUrl = getCurrentUrlPath();

        String listByTagPageBaseUrl = "/templates/usr/" + domainName + "/listByTag";

        if (currentUrl.startsWith("/templates/usr/" + domainName + "/list")) return listByTagPageBaseUrl;
        if (currentUrl.startsWith("/templates/usr/" + domainName + "/listByTag")) return listByTagPageBaseUrl;

        String listUrl = getParam("listUrl", "");

        if (currentUrl.startsWith("/templates/usr/" + domainName + "/detail") && listUrl.isBlank()) return listByTagPageBaseUrl;
        if (listUrl.startsWith("/templates/usr/" + domainName + "/list")) return listByTagPageBaseUrl;
        if (listUrl.startsWith("/templates/usr/" + domainName + "/listByTag")) return listByTagPageBaseUrl;

        return "/templates/usr/post/myListByTag";
    }
}
