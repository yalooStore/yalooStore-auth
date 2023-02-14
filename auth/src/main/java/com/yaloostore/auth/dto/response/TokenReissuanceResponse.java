package com.yaloostore.auth.dto.response;

import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenReissuanceResponse {
    private String accessToken;
    private String refreshToken;
}
