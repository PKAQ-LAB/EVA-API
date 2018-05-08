package org.pkaq.sys.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.user.entity.UserEntity;
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
     * 根据用户userId 获取包含权限列表的用户信息
     * @param userId
     * @return
     */
    UserEntity getUserWithRoleById(String userId);
}
