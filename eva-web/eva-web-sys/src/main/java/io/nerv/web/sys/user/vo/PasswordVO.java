package io.nerv.web.sys.user.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PasswordVO {
    private String originpassword;
    private String newpassword;
}
