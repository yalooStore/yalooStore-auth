package com.yaloostore.auth.jwt;


import com.yaloostore.auth.dto.response.TokenReissuanceResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;


/**
 * JWT Token 생성을 위한 클래스입니다.
 * 유저 정보를사용해서 JWT 토큰을 만들거나 토큰을 바탕으로 유저 정보를 가져옵니다.
 * */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = Duration.ofMinutes(30).toMillis();
    private static final long REFRESH_TOKEN_EXPIRE_TIME = Duration.ofDays(7).toMillis();


    // spring security에서 유저 정보를 가져오는 인터페이스
    private final UserDetailsService userDetailsService;

    @Value("${jwt.secretKey}")
    private String secretKey;

    /**
     * JWT 생성을 위한 HMAC - SHA 알고리즘으로 JWT에 서명키를 생성해주는 메소드
     *
     * @param secretKey JWT를 생성하기 위해서 사용하는 secretKey
     * @return 인코딩된 secretKey 기반으로 HMAC - SHA 알고리즘으로 생성한 Key 반환
     * */
    private Key getSecretKey(String secretKey){
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String loginId, List<String> roles, long tokenExpireTime){
        Claims claims = Jwts.claims().setSubject(loginId);
        claims.put("roles", roles);
        Date date = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime()+ tokenExpireTime))
                .signWith(getSecretKey(secretKey), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * access token 발급 메소드
     * @param roles 회원 권한 목록
     * */
    public String createAccessToken(String loginId,
                                    List<String> roles){
        return createToken(loginId, roles, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     * Refresh token 발급 메소드
     * @param roles 회원 권한 목록
     * */
    public String createRefreshToken(String loginId,
                                    List<String> roles){

        return createToken(loginId, roles, REFRESH_TOKEN_EXPIRE_TIME);
    }

    /**
     * JWT 토큰 파싱하여 payload에 있는 회원의 loginId 반환하는 메소드
     * @param token JWT 토큰
     * @return payload(패킷)에 들어있는 회원의 loginId
     * */
    public String extractLoginId(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    /**
     * secretKey를 기반으로 JWT 토큰이 유효한지를 검증하는 메소드
     * @return 유효할 경우 true, 유효하지 않을 경우 false
     * */
    public boolean isValidToken(String token){
        try {

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey(secretKey))
                    .build()
                    .parseClaimsJws(token);

        } catch (Exception e){
            return false;
        }
        return true;
    }

    public TokenReissuanceResponse tokenReissuance(String loginId, List<String> roles){
        String accessToken = createAccessToken(loginId, roles);
        String refreshToken = createRefreshToken(loginId, roles);

        return new TokenReissuanceResponse(accessToken, refreshToken);
    }

    public Authentication getAuthentication(String token){
        UserDetails userDetails = userDetailsService.loadUserByUsername(extractLoginId(token));
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }






}
