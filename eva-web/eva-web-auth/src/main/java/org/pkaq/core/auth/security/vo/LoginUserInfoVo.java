package org.pkaq.core.auth.security.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录用户信息
 *
 * @author PKAQ
 */
@Data
public class LoginUserInfoVo {
    private Long id;
    private String account;
    private String username;
    private Long deptId;
    private String deptName;
    private String name;
    private String nickName;
    private List<String> authorities;
}
