package com.yk.Motivation.base.security;

import com.yk.Motivation.standard.util.Ut;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;

public class CustomSimpleUrlAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        SavedRequest savedRequest = this.requestCache.getRequest(request, response); // 사용자가 로그인 페이지로 redirect 되기 전 요청

        clearAuthenticationAttributes(request); // 세션에 임시로 저장되었던 인증 관련 속성 정리

        String targetUrl = savedRequest != null ? savedRequest.getRedirectUrl() : getDefaultTargetUrl();

        targetUrl = Ut.url.modifyQueryParam(targetUrl, "msg", Ut.url.encodeWithTtl("환영합니다."));

        getRedirectStrategy().sendRedirect(request, response, targetUrl); // redirect
    }
}
