package tech.yunyue.auth.domain;

import cn.hutool.core.util.StrUtil;
import tech.yunyue.core.enums.LockEnumm;
import tech.yunyue.core.threaduser.ThreadUser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JwtUser 工厂
 * @author PKAQ
 */
public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUserDetail create(Map<String, Object> userMap,Map<String, ThreadUser.GrantedRoles> rolesMap) {
        return new JwtUserDetail(
                StrUtil.toStringOrNull(userMap.get("ID")),
                StrUtil.toStringOrNull(userMap.get("ACCOUNT")),
                StrUtil.toStringOrNull(userMap.get("TEL")),
                StrUtil.toStringOrNull(userMap.get("PASSWORD")),
                LockEnumm.LOCK.getCode().equals(StrUtil.toStringOrNull(userMap.get("LOCKED"))),
                StrUtil.toStringOrNull(userMap.get("DEPT_ID")),
                StrUtil.toStringOrNull(userMap.get("DEPT_NAME")),
                StrUtil.toStringOrNull(userMap.get("NAME")),
                StrUtil.toStringOrNull(userMap.get("NICK_NAME")),
                StrUtil.toStringOrNull(userMap.get("TENANT_ID")),
                StrUtil.toStringOrNull(userMap.get("U_POST_ID")),
                StrUtil.toStringOrNull(userMap.get("POST_NAME")),
                rolesMap
        );
    }

    public static Map<String, ThreadUser.GrantedRoles> mapToGrantedAuthorities(List<Map<String, Object>> roleList) {
        Map<String, ThreadUser.GrantedRoles> roles = roleList.stream()
                .collect(Collectors.toMap(
                        o->StrUtil.toStringOrNull(o.get("ID")),
                        o->new ThreadUser.GrantedRoles(
                                StrUtil.toStringOrNull(o.get("NAME")),
                                StrUtil.toStringOrNull(o.get("CODE")),
                                StrUtil.toStringOrNull(o.get("DATA_PERMISSION_TYPE")),
                                StrUtil.toStringOrNull(o.get("DATA_PERMISSION_DEPTID")))));
        return roles;
    }
}
