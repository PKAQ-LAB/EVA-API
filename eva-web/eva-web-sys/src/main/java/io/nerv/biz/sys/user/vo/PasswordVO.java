package io.nerv.biz.sys.user.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PasswordVO {
    private String userId;

    private String originpassword;

    private String newpassword;
}
