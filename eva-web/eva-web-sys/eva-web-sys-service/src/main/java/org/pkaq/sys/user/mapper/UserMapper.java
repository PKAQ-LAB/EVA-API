package org.pkaq.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.core.mybatis.util.PageResult;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.List;
import java.io.Serializable;

/**
 * 用户管理Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<UserEntity> {

    /** 详情左联管理档案，纯注册账号也可管理。 */
    @Override
    UserEntity selectById(@Param("id") Serializable id);

    /** 查询账号与可选管理档案列表。 */
    List<UserEntity> selectManagedList(@Param("query") UserQueryBo query, @Param("postIds") List<Long> postIds);

    /** 分页查询账号与可选管理档案。 */
    PageResult<UserEntity> selectManagedPage(PageResult<UserEntity> page,
            @Param("query") UserQueryBo query, @Param("postIds") List<Long> postIds);

    /** 查询账号或档案工号重复项。 */
    @Ignore
    Long countDuplicate(@Param("account") String account, @Param("code") String code, @Param("id") Long id);

    /** 统计指定部门的未删除账号。 */
    @Ignore
    Long countDepartmentAccounts(@Param("ids") Set<Long> ids);

    /** 检查只读账号及受保护工号。 */
    @Ignore
    Long countReadOnlyAccounts(@Param("ids") Set<Long> ids);

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
     * 自增用户权限版本号
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE SYS_ACCOUNT SET PERM_VER = COALESCE(PERM_VER, 0) + 1 WHERE ID = #{userId} AND DELETED = 0")
    int incrementPermVer(@Param("userId") Long userId);
}
