package com.yaloostore.auth.domain.customclass;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;

import java.io.Serializable;
import java.util.List;

/**
 * 레디스에 저장할 때 직렬화해서 저장하기위해서 사용하는 클래스입니다.
 *
 * 직렬화해서 사용할 경우엔 이렇게 serializable을 implements 해서 사용해야 한다.
 * */
@Getter
@NoArgsConstructor
public class AuthMember implements Serializable {
    private String loginId;
    private List<String> roles;
    private String accessToken;
    private String expiredTime;

    public AuthMember(Authentication authentication,List<String> roles,
                      String accessToken, String expiredTime){
        this.loginId = authentication.name();
        this.roles = roles;
        this.accessToken = accessToken;
        this.expiredTime = expiredTime;
    }
}
