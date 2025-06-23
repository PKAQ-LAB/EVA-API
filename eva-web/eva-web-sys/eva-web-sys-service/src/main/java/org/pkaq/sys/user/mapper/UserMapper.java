package org.pkaq.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户管理mapper
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 查询用户信息
     */
    @Ignore
    IPage<UserListVo> getUerWithRoleId(IPage page, @Param("user") UserAoeBo user);

    /**
     * 根据用户account 获取包含权限列表的用户信息
     */
    @Ignore
    UserDetailVo getUserWithRole(UserEntity user);

    /**
     * 根据用户userId 获取包含权限列表 菜单列表的用户信息
     */
    @Ignore
    UserEntity getUserWithModuleAndRoleById(String userId);

    /**
     * 切换锁定状态
     *
     * @param ids
     */
    void change(List<String> ids);
}
