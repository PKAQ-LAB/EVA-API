package org.pkaq.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.user.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 用户管理Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户ID获取包含模块与角色的用户信息
     */
    @Ignore
    UserEntity getUserWithModuleAndRoleById(String userId);

    /**
     * 切换锁定状态
     */
    void change(Set<Long> ids);

    /**
     * 查询剩余可用授权用户数
     */
    Integer availableCounts(long tid);

    /**
     * 自增用户权限版本号
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE SYS_USER SET PERM_VER = COALESCE(PERM_VER, 0) + 1 WHERE ID = #{userId} AND DELETED = 0")
    int incrementPermVer(@Param("userId") Long userId);
}
