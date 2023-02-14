package com.yaloostore.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;


/**
 * JWT 인증 로직 수행 시 로그인에 실패하는 경우 해당 클래스(핸들러)를 사용해서 처리할 때 사용합니다.
 * */

@Slf4j
public class JwtFailureHandler implements AuthenticationFailureHandler {
    @Value("${yalooStore.front}")
    private String frontUrl;


    /**
     * 인증관련 요청이 실패한 경우 다시 login form 으로 redirect 시켜주는 메소드
     * */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("Authentication Failer Handler called");
        response.sendRedirect(frontUrl+"/members/login");
    }
}
