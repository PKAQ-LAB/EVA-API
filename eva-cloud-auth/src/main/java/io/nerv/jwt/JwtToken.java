package io.nerv.jwt;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
public class JwtToken {
    private String accessToken;
    private String refreshToken;
}
