package com.yaloostore.auth.service.inter;


import com.yalooStore.security_utils.dto.AuthorizationResponseDto;

public interface AuthorizationService {

    AuthorizationResponseDto authorization(String token);
}
