package com.yaloostore.auth.controller;


import com.yalooStore.common_utils.dto.ResponseDto;
import com.yaloostore.auth.exception.InvalidAuthorizationHeaderException;
import com.yaloostore.auth.jwt.JwtProvider;
import com.yaloostore.auth.service.inter.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.yaloostore.auth.utils.AuthUtil.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthenticationService authenticationService;



    @PostMapping("/reissue")
    public ResponseDto<Void> tokenReissue(HttpServletRequest request, HttpServletResponse response){
        String accessToken = request.getHeader(AUTHORIZATION);
        String uuid = request.getHeader(HEADER_UUID.getValue());
        if(isValidHeader(accessToken,uuid)){
            throw new InvalidAuthorizationHeaderException();
        }
        //해당 회원이 가지고 있는 고유 uuid가 올바르지 않은 값일 때 response
        if (isNotValidKey(uuid)){
            return ResponseDto.<Void>builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .success(false)
                    .errorMessages(List.of("유효하지 않은 회원입니다."))
                    .build();
        }

        if(isValidRefreshToken(uuid)) {
            return ResponseDto.<Void>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .errorMessages(List.of("refresh token 만료!"))
                    .build();
        }

        String loginId = authenticationService.getLoginId(uuid);
        String principal = authenticationService.getPrincipal(uuid);

        log.info("roles : {}", principal);

        List<String> roles = extractMemberRoles(principal);
        String reissueToken = jwtProvider.reissueToken(loginId, roles);
        authenticationService.doReissue(uuid, reissueToken);

        long expiredTime = jwtProvider.extractExpiredTime(reissueToken).getTime();

        response.addHeader(AUTHORIZATION, reissueToken);
        response.addHeader(HEADER_UUID.getValue(), uuid);
        response.addHeader(HEADER_EXPIRED_TIME.getValue(), String.valueOf(expiredTime));

        return ResponseDto.<Void>builder()
                .success(true)
                .status(HttpStatus.OK)
                .build();


    }

    private List<String> extractMemberRoles(String principal) {
        return Arrays.asList(principal.replaceAll("[\\[\\]]", "").split(", "));
    }

    private boolean isValidRefreshToken(String uuid) {

        String refreshToken= Objects.requireNonNull(redisTemplate.opsForHash().get(uuid, REFRESH_TOKEN.getValue())).toString();

        long expiredTime = jwtProvider.extractExpiredTime(refreshToken).getDate();
        long now = new Date().getTime();

        return (expiredTime - (now/1000)) > 0;
    }


    private boolean isValidHeader(String accessToken, String uuid) {
        return Objects.isNull(accessToken) || Objects.isNull(uuid) || !accessToken.startsWith("Bearer ") || jwtProvider.isValidToken(accessToken);

    }

    /**
     * 해당 키값에 해당하는 정보가 없다면 유효하지 않은 키로 판단합니다.
     * @return 올바르지 않은 key = true 올바른 키 = false
     * */
    private boolean isNotValidKey(String uuid) {
        return redisTemplate.opsForHash().keys(uuid).isEmpty();
    }
}
