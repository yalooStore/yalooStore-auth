package com.yaloostore.auth.service.impl;

import com.yalooStore.common_utils.code.ErrorCode;
import com.yalooStore.common_utils.dto.ResponseDto;
import com.yalooStore.common_utils.exception.ClientException;
import com.yaloostore.auth.config.ServerMetaDataConfig;
import com.yaloostore.auth.domain.dto.response.MemberLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RestTemplate restTemplate;
    private final ServerMetaDataConfig serverMetaDataConfig;


    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        HttpHeaders headers = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(headers);


        UriComponents uri = UriComponentsBuilder.fromHttpUrl(serverMetaDataConfig.getShopUrl()+"/api/service/members/login/"+loginId).build();

        ResponseEntity<ResponseDto<MemberLoginResponse>> memberLoginResponse = restTemplate
                .exchange(uri.toUri(),
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<ResponseDto<MemberLoginResponse>>() {
                });


        //해당하는 로그인 아이디를 가진 회원이 없으면 에러 던짐
        MemberLoginResponse data = memberLoginResponse.getBody().getData();
        if (Objects.isNull(data)){
            throw new ClientException(ErrorCode.MEMBER_NOT_FOUND,"not found member");
        }

        User user = new User(data.getLoginId(), data.getPassword(), getAuthorities(data));

        return user;
    }

    private List<? extends GrantedAuthority> getAuthorities(MemberLoginResponse data) {
        List<SimpleGrantedAuthority> authorities = data.getRoles()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return authorities;
    }


}
