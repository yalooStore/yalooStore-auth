package com.yaloostore.auth.service.inter;


/**
 * 인증과 관련된 서비스 로직을 처리하는 클래스입니다.
 *
 * 예를 들면 redis(in-memory 기반의 데이터베이스)를 사용하고 있으니 해당 데이터베이스에서 정보를 가져오는 등의 작업을 진행합니다.
 * */
public interface AuthenticationService {

    /**
     * uuid 값을 가지고 해당 회원의 로그인 아이디를 레디스 내에서 가져옵니다.
     *
     * @param uuid 해당 회원의 고유 uuid
     * @return 회원 로그인 아이디값
     * */
    String getLoginId(String uuid);


    /**
     * uuid 값을 가지고 해당 회원의 권한을 레디스 내에서 가져옵니다.
     *
     * @param uuid 해당 회원의 고유 uuid
     * @return 회원 권한
     * */
    String getPrincipal(String uuid);


    /**
     * 회원 고유 uuid값으로 해당 accessToken을 삭제하고 다시 발급받은 accessToken을 레디스에 저장해줍니다.
     *
     * @param uuid 회원 고유 uuid
     * @param accessToken 새롭게 발급 받은 accessToken
     * */
    void doReissue(String uuid, String accessToken);

    /**
     * 회원 고유 uuid값으로 redis에 저장된 jwt 토큰을 삭제합니다.
     *
     * @param uuid 회원 고유 uuid
     * */
    void doLogout(String uuid);
}
