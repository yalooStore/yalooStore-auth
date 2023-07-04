package com.yaloostore.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yalooStore.common_utils.code.ErrorCode;
import com.yalooStore.common_utils.exception.ClientException;
import com.yaloostore.auth.domain.dto.request.MemberLoginRequest;
import com.yaloostore.auth.exception.InvalidLoginRequestException;
import com.yaloostore.auth.jwt.JwtProvider;
import com.yaloostore.auth.member.dto.MemberLoginHistoryResponse;
import com.yaloostore.auth.member.service.inter.MemberLoginHistoryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.yaloostore.auth.utils.AuthUtil.*;


@Slf4j
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationManager authenticationManager;

    private final MemberLoginHistoryService memberLoginHistoryService;


    private static final AntPathRequestMatcher DEFAULT_FORM_LOGIN_REQUEST_MATCHER = new AntPathRequestMatcher("/auth/login", "POST");

    private final JwtProvider jwtProvider;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";


    public JwtAuthenticationFilter(AuthenticationManager authenticationManage, MemberLoginHistoryService memberLoginHistoryService, JwtProvider jwtProvider, RedisTemplate<String, Object> redisTemplate) {
        super(DEFAULT_FORM_LOGIN_REQUEST_MATCHER);
        this.authenticationManager = authenticationManage;
        this.memberLoginHistoryService = memberLoginHistoryService;

        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
    }


    /**
     * front 서버에서 넘어온 회원정보를 이용해서 해당 authentication 객체를 만들어 다음 작업으로 넘겨주는 메소드입니다.
     *
     * front -> request(loginId, password) -> authentication생성 -> authenticationManager로 위임
     * */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        log.info("================ auth server get HttpServletRequest start ================");
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


    /**
     * 해당 사용자가 인증에 성공하면 해당 메소드가 실행됩니다.
     * */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication auth) throws IOException, ServletException {


        if (Objects.isNull(auth)){
            throw new ClientException(ErrorCode.MEMBER_NOT_FOUND, "member is not found");
        }
        String loginId = auth.getName();
        List<String> authorities = getAuthorities(auth.getAuthorities());

        //로그인 성공했으니까 해당 아이디를 가지고 로그인 기록을 남기기
        MemberLoginHistoryResponse memberLoginHistoryResponse = memberLoginHistoryService.saveLoginHistory(loginId);
        log.info("login history put date time? : {}", memberLoginHistoryResponse.getLoginTime());

        String accessToken = jwtProvider.createAccessToken(loginId, authorities);
        String refreshToken = jwtProvider.createRefreshToken(loginId, authorities);
        Date expiredTime = jwtProvider.extractExpiredTime(accessToken);

        String memberUuid = UUID.randomUUID().toString();

        redisTemplate.opsForHash().put(memberUuid, ACCESS_TOKEN.getValue(),accessToken);
        redisTemplate.opsForHash().put(memberUuid, REFRESH_TOKEN.getValue(),refreshToken);
        redisTemplate.opsForHash().put(memberUuid, LOGIN_ID.getValue(),loginId);
        redisTemplate.opsForHash().put(memberUuid, PRINCIPAL.getValue(),auth.getAuthorities().toString());


        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX+accessToken);
        response.addHeader(HEADER_UUID.getValue(), memberUuid);
        response.addHeader(HEADER_EXPIRED_TIME.getValue(), expiredTime.toString());

    }

    private List<String> getAuthorities(Collection<? extends GrantedAuthority> authorities){
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }


}
