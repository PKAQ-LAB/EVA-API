package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.role.entity.RoleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色管理mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface RoleMapper extends BaseMapper<RoleEntity> {
    /**
     * 根据UserID查询角色权限
     *
     * @param userId
     * @return
     */
    @Ignore
    List<RoleEntity> selectByUserId(String userId);
}
