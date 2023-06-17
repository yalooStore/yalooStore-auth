package com.yaloostore.auth.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaloostore.auth.domain.dto.request.MemberLoginRequest;
import com.yaloostore.auth.exception.InvalidHttpMethodRequestException;
import com.yaloostore.auth.exception.InvalidLoginRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Objects;


public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationManager authenticationManager;

    private static final String LOGIN_ID_PARAMETER = "loginId";
    private static final String PASSWORD_PARAMETER = "password";

    private static final AntPathRequestMatcher DEFAULT_FORM_LOGIN_REQUEST_MATCHER = new AntPathRequestMatcher("/auth/login", "POST");


    public JwtAuthenticationFilter(AuthenticationManager authenticationManage) {
        super(DEFAULT_FORM_LOGIN_REQUEST_MATCHER);
        this.authenticationManager = authenticationManage;
    }


    /**
     * front 서버에서 넘어온 회원정보를 이용해서 해당 authentication 객체를 만들어 다음 작업으로 넘겨주는 메소드입니다.
     *
     * front -> request(loginId, password) -> authentication생성 -> authenticationManager로 위임
     * */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        ObjectMapper objectMapper =new ObjectMapper();

        MemberLoginRequest memberLoginRequest;


        try {
            memberLoginRequest = objectMapper.readValue(request.getInputStream(), MemberLoginRequest.class);
        } catch (IOException e){
            throw new InvalidLoginRequestException();
        }

        String loginId = memberLoginRequest.getLoginId();
        String password = memberLoginRequest.getPassword();


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
