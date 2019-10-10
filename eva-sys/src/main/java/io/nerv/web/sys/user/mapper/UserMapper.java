package io.nerv.web.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.nerv.web.sys.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 用户管理mapper
 * @author: S.PKAQ
 * @Datetime: 2018/3/29 23:57
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 查询用户信息
     * @param user
     * @param page
     * @return
     */
    IPage<UserEntity> getUerWithRoleId(IPage page, @Param("user") UserEntity user);

    /**
     * 根据用户account 获取包含权限列表的用户信息
     * @return
     */
    UserEntity getUserWithRole(UserEntity user);

    /**
     * 根据用户userId 获取包含权限列表 菜单列表的用户信息
     * @param userId
     * @return
     */
    UserEntity getUserWithModuleAndRoleById(String userId);
}
