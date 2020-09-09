package io.nerv.domain;

import io.nerv.core.security.domain.JwtUserDetail;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class AuthEntity {
    /**
     * 访问令牌
     */
    private String tk_alpha;
    /**
     * 刷新令牌
     */
    private String tk_bravo;

    /**
     * 用户对象
     */
    private JwtUserDetail jwtUserDetail;
}
