package com.example.demo.base.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ApplicationContext context;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // authorizeRequests id deprecated -> authorizeHttpRequests

        // 문제 발생
        // 이전 버전에서는 .access 메서드에서  SpEL 표현식을 통해 스프링 빈에 직접 접근이 가능했었다.
        // WebApplicationInitializer 를 사용 하여 ContextLoaderListener 를 추가하는 방식이 Spring Security 6.1.2 와 Tomcat 10.1를 함께 사용할 때 문제를 일으켰다.
        // 때문에 ApplicationContext 를 직접 주입 받아서 context 필드를 통해 사용하게 했다.
        // https://github.com/spring-projects/spring-security/issues/13609


        http
                .authorizeHttpRequests(
                        authorizeRequests -> authorizeRequests
                                .requestMatchers(requestMatchersOf("/templates/usr/member/notVerified")
                                ).permitAll()
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/post/modify/*")
                                ).access(accessOf("@postController.assertActorCanModify()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/post/remove/*")
                                ).access(accessOf("@postController.assertActorCanRemove()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/article/*/write")
                                ).access(accessOf("@articleController.assertActorCanWrite()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/article/*/modify/*")
                                ).access(accessOf("@articleController.assertActorCanModify()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/article/*/remove/*")
                                ).access(accessOf("@articleController.assertActorCanRemove()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/book/*/write")
                                ).access(accessOf("@bookController.assertActorCanWrite()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/book/*/modify/*")
                                ).access(accessOf("@bookController.assertActorCanModify()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/book/*/remove/*")
                                ).access(accessOf("@bookController.assertActorCanRemove()"))
                                .requestMatchers(
                                        requestMatchersOf("/templates/usr/member/beProducer", "/templates/usr/member/modify")
                                ).access(accessOf("@memberController.assertCheckPasswordAuthCodeVerified()"))
                                .requestMatchers(
                                        requestMatchersOf("/", "/templates/usr/**")
                                ).access(accessOf("isAnonymous() or @memberController.assertCurrentMemberVerified()"))
                                .requestMatchers(
                                        requestMatchersOf("/adm/**")
                                )
                                .hasAuthority("admin")
                                .anyRequest().permitAll()
                )
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login
                                .loginPage("/templates/usr/member/login")
                )
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .formLogin((formLogin) -> formLogin
                        .loginPage("/templates/usr/member/login")
                        .successHandler(new CustomSimpleUrlAuthenticationSuccessHandler())
                        .failureHandler(new CustomSimpleUrlAuthenticationFailureHandler())
                )
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/templates/usr/member/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true))
        ;
        return http.build();
    }

    private WebExpressionAuthorizationManager accessOf(String expressionString) {
        DefaultHttpSecurityExpressionHandler expressionHandler = new DefaultHttpSecurityExpressionHandler();
        expressionHandler.setApplicationContext(context);
        WebExpressionAuthorizationManager authorization = new WebExpressionAuthorizationManager(expressionString);
        authorization.setExpressionHandler(expressionHandler);

        return authorization;
    }

    private AntPathRequestMatcher[] requestMatchersOf(String... patterns) {
        return Stream.of(patterns)
                .map(AntPathRequestMatcher::new)
                .toArray(AntPathRequestMatcher[]::new);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

//    public LogoutHandler oAuth2LogoutHandler() {
//        return (request, response, authentication) -> {
//            if (authentication instanceof OAuth2AuthenticationToken) {
//                String kakaoLogoutURL = "https://kauth.kakao.com/oauth/logout?client_id=61f551ef34c13cdb5bf18c6fa42e4d20&logout_redirect_uri=https://localhost:8090/usr/member/logout";
//                try {
//                    response.sendRedirect(kakaoLogoutURL);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };
//    }
