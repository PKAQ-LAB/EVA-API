package io.nerv.web.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.annotation.Ignore;
import org.apache.ibatis.annotations.Mapper;
import io.nerv.web.sys.role.entity.RoleEntity;
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
    @Ignore
    List<RoleEntity> selectByUserId(String userId);

    /**
     * 根据角色编码获取数据权限
     * @return
     */
    List<RoleEntity> selectDataPermission(List<String> roleCodes);
}
