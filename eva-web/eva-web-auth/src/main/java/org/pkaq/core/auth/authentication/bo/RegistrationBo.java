package org.pkaq.core.auth.authentication.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.ToString;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 自助注册仅接收账号、原始密码和昵称，不接收管理授权字段。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
public class RegistrationBo implements Bo {
    private String account;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;
    private String nickName;

    /** 拒绝全部未声明字段，异常不包含字段值或凭据。 */
    @JsonAnySetter
    public void rejectUnknownField(String field, Object value) {
        throw new IllegalArgumentException("注册仅允许账号、密码和昵称字段");
    }
}
