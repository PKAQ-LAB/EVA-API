package org.pkaq.core.auth.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 认证角色资源Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface AuthRoleResourceMapper extends BaseMapper<Object> {
    /**
     * 根据URL返回匹配的权限名称
     *
     * @return 角色编码与路径列表
     */
    List<Map<String, String>> listRoleNamesWithPath();
}
