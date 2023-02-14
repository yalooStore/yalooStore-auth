package com.yaloostore.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaloostore.auth.dto.request.LoginRequest;
import com.yaloostore.auth.exception.InvalidLoginRequestException;
import com.yaloostore.auth.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.yaloostore.auth.utill.AuthUtil.*;


/**
 * JWT 토큰 인증을 위해 커스텀한 필터 클래스
 * */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";
    private static final String UUID_HEADER = "UUID_HEADER";


    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;


    /**
     * front server에서 작동하는 메소드로 사용자가 입력한 아이디 비밀번호를 기반으로 작동합니다.
     * UsernamePasswordAuthenticationToken 발급과 authenticationManager에게 인가를 위임합니다.
     *
     * */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        ObjectMapper mapper = new ObjectMapper();
        LoginRequest loginRequest;


       try {
           loginRequest = mapper.readValue(request.getInputStream(), LoginRequest.class);
       } catch (IOException e){
           throw new InvalidLoginRequestException();

       }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getId(),
                loginRequest.getPassword()

        );

        return authenticationManager.authenticate(authenticationToken);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String accessToken = getAccessToken(auth);
        String refreshToken = getRefreshToken(auth);


        String memberUUID = UUID.randomUUID().toString();

        redisTemplate.opsForHash().put(memberUUID, REFRESH_TOKEN.getValue(), refreshToken);
        redisTemplate.opsForHash().put(memberUUID, ACCESS_TOKEN.getValue(), accessToken);
        redisTemplate.opsForHash().put(memberUUID, USER_ID.getValue(), auth.getName());
        redisTemplate.opsForHash().put(memberUUID, PRINCIPALS.getValue(), auth.getAuthorities().toString());


        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        response.addHeader(UUID_HEADER, memberUUID);
    }


    /**
     * 인증객체를 jwtTokenProvider로 전달해서 AccessToken이나 RefreshToken을  발급
     *
     * @param  auth 인증 객체
     * @return JWT 형식의 AccessToken
     * */
    private String getAccessToken(Authentication auth){
        return jwtTokenProvider.createAccessToken(auth.getName(),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }
    private String getRefreshToken(Authentication auth){
        return jwtTokenProvider.createRefreshToken(auth.getName(),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }


    /**
     * 인증 실패 시 동작하는(후처리에 사용) 메소드
     * */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}
