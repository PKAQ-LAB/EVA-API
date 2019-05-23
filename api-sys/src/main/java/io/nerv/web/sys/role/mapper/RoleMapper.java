package io.nerv.web.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.web.sys.role.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色管理mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:25
 */
@Mapper
@Repository
public interface RoleMapper extends BaseMapper<RoleEntity> {
    /**
     * 根据UserID查询角色权限
     * @param userId
     * @return
     */
    List<RoleEntity> selectByUserId(String userId);

    /**
     * 根据角色信息和用户account查询角色
     * @param page
     * @param entity
     * @return
     */
    IPage<RoleEntity> selectRoleByUserId(@Param("page") Page<RoleEntity> page , @Param("entity") RoleEntity entity,@Param("account") String account);
}
