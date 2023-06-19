package com.yaloostore.auth.domain.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 인증, 인가 관련에서 로그인시에 필요한 회원 정보를 담은 dto 객체입니다.
 * */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginResponse {


    private Long memberId;
    private String loginId;
    private String name;
    private String nickname;
    private String email;
    private String password;
    private List<String> roles;



}
