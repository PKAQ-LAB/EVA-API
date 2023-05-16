package tech.yunyue.domain;

import cn.hutool.core.bean.BeanUtil;
import tech.yunyue.core.enums.LockEnumm;
import tech.yunyue.sys.role.entity.RoleEntity;
import tech.yunyue.sys.user.entity.UserEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JwtUser 工厂
 * @author PKAQ
 */
public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUserDetail create(UserEntity user) {
        return new JwtUserDetail(
                String.valueOf(user.getId()),
                user.getAccount(),
                user.getPassword(),
                user.getDeptId(),
                user.getDeptName(),
                user.getName(),
                user.getNickName(),
                LockEnumm.LOCK.getCode().equals(user.getLocked()),
                mapToGrantedAuthorities(user.getRoles())
        );
    }

    private static Map<String,GrantedRoles> mapToGrantedAuthorities(List<RoleEntity> authorities) {
        Map<String,GrantedRoles> rolesMap=new HashMap<>(authorities.size());
        authorities.stream()
                .forEach(item -> {
                    GrantedRoles grantedRoles = new GrantedRoles();
                    BeanUtil.copyProperties(item, grantedRoles);
                    rolesMap.put(item.getCode(),grantedRoles);
                });
        return rolesMap;
    }
}
