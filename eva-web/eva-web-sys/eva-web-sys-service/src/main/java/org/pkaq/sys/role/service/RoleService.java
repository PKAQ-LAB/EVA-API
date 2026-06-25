package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleResourceEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.vo.RoleGrantedModuleVo;
import org.pkaq.sys.role.vo.RoleGrantedUserVo;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class RoleService extends StdService<RoleMapper, RoleEntity> implements IRoleService {

    private final RoleResourceMapper roleResourceMapper;

    private final RoleUserMapper roleUserMapper;

    private final ModuleMapper moduleMapper;

    private final UserMapper userMapper;

    private final UserConvert userConvert;

    /**
     * 批量删除角色：
     * - 同步清理 角色-用户、角色-资源 中间表
     * - 自增受影响用户的权限版本号（让其下一次请求重新拉权限）
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }

        // 删除前收集受影响用户
        Set<Long> affectedUsers = fetchUsersByRoleIds(ids);

        // 删除 角色-用户 中间表
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().in(RoleUserEntity::getRoleId, ids));
        // 删除 角色-资源 中间表
        this.roleResourceMapper.delete(new LambdaQueryWrapper<RoleResourceEntity>().in(RoleResourceEntity::getRoleId, ids));
        // 删除角色本体
        this.mapper.deleteByIds(ids);

        // 自增受影响用户的权限版本号
        incrementPermVer(affectedUsers);
    }

    /**
     * 校验编码唯一性（同名 / 同 code）
     * 注意：RoleEntity.setCode 已统一规范化为 ROLE_ 前缀 + 大写，
     * 此处只做查询比对，不再二次拼接前缀。
     */
    @Override
    public boolean isUnique(IdCodeBo idCodeBo) {
        if (idCodeBo == null || idCodeBo.getCode() == null) {
            return false;
        }
        // 规范化：与 setCode 同样的规则
        String code = idCodeBo.getCode().trim();
        if (!code.toUpperCase().startsWith(CommonConstant.AUTH_PREFIX)) {
            code = CommonConstant.AUTH_PREFIX + code;
        }
        code = code.toUpperCase();

        var wrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getCode, code)
                .ne(idCodeBo.getId() != null && idCodeBo.getId() != 0L, RoleEntity::getId, idCodeBo.getId());

        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取角色绑定的所有模块资源
     */
    @Override
    public RoleGrantedModuleVo fetchResource(RoleResourceRefBo roleModule) {
        if (roleModule == null || roleModule.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return null;
        }
        var curUid = ThreadUserHelper.getUserId();
        var roleId = roleModule.getRoleId();

        Map<Long, ModuleDetailVo> moduleMap = this.moduleMapper.listGrantedModules(curUid);
        Map<Long, List<org.pkaq.sys.module.vo.ModuleResourcesVo>> resourceMap = this.roleResourceMapper.listGrantedResource(roleId);

        Set<Long> moduleChecked = new HashSet<>();
        // 将资源装配到模块；防御性 NPE：moduleMap 中可能没有对应模块（被删/无权限）
        if (CollUtils.isNotEmpty(resourceMap)) {
            resourceMap.forEach((moduleId, resources) -> {
                ModuleDetailVo module = moduleMap.get(moduleId);
                if (module != null) {
                    module.setResources(resources);
                    moduleChecked.add(moduleId);
                }
            });
        }

        var moduleTree = TreeHelper.buildTree(moduleMap.values());

        RoleGrantedModuleVo vo = new RoleGrantedModuleVo();
        vo.setModules(moduleTree);
        vo.setCheckedModuleIds(moduleChecked);
        return vo;
    }

    /**
     * 保存角色-资源关系（diff 模式）
     * - 新增的资源 id 插入
     * - 已存在但不在新集合的删除
     * - 保留交集（避免无谓 IO）
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void grantResource(RoleResourceRefBo role) {
        if (role == null || role.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        Long roleId = role.getRoleId();

        // 当前已授权的 resource id 集合
        Set<Long> existing = this.roleResourceMapper.selectList(
                        new LambdaQueryWrapper<RoleResourceEntity>().eq(RoleResourceEntity::getRoleId, roleId))
                .stream()
                .map(RoleResourceEntity::getResourceId)
                .collect(Collectors.toSet());

        Set<Long> incoming = CollUtils.isEmpty(role.getResourceId()) ? new HashSet<>() : new HashSet<>(role.getResourceId());

        // 待新增 = incoming - existing
        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);
        // 待删除 = existing - incoming
        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        if (!toDelete.isEmpty()) {
            this.roleResourceMapper.delete(new LambdaQueryWrapper<RoleResourceEntity>()
                    .eq(RoleResourceEntity::getRoleId, roleId)
                    .in(RoleResourceEntity::getResourceId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long rid : toInsert) {
                RoleResourceEntity ref = new RoleResourceEntity();
                ref.setRoleId(roleId);
                ref.setResourceId(rid);
                this.roleResourceMapper.insert(ref);
            }
        }

        // 该角色下的所有用户都需要刷新权限版本号
        incrementPermVer(fetchUsersByRoleIds(Set.of(roleId)));
    }

    /**
     * 获取角色绑定的所有用户（指定部门下）
     */
    @Override
    public RoleGrantedUserVo listUser(Long roleId, Long deptId) {
        LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(deptId != null && deptId != 0L, UserEntity::getDeptId, deptId);
        userWrapper.eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN);

        List<UserEntity> users = this.userMapper.selectList(userWrapper);

        LambdaQueryWrapper<RoleUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleUserEntity::getRoleId, roleId);
        wrapper.select(RoleUserEntity::getUserId);

        Set<Long> checkedUser = this.roleUserMapper.selectObjs(wrapper)
                .stream()
                .map(o -> (Long) o)
                .collect(Collectors.toSet());

        RoleGrantedUserVo vo = new RoleGrantedUserVo();
        vo.setCheckedUser(checkedUser);
        vo.setUsers(this.userConvert.entityToSimpleVo(users));
        return vo;
    }

    /**
     * 保存角色-用户关系（diff 模式 + 事务）
     * 删除前后受影响用户均自增权限版本号
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void grantUser(RoleUserRefBo role) {
        if (role == null || role.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        Long roleId = role.getRoleId();

        // 受影响用户 = 旧 ∪ 新（无论增加还是减少都要刷 permVer）
        Set<Long> oldUsers = fetchUsersByRoleIds(Set.of(roleId));
        Set<Long> incoming = CollUtils.isEmpty(role.getUserId()) ? new HashSet<>() : new HashSet<>(role.getUserId());
        Set<Long> affectedUsers = new HashSet<>(oldUsers);
        affectedUsers.addAll(incoming);

        // diff
        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(oldUsers);
        Set<Long> toDelete = new HashSet<>(oldUsers);
        toDelete.removeAll(incoming);

        if (!toDelete.isEmpty()) {
            this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>()
                    .eq(RoleUserEntity::getRoleId, roleId)
                    .in(RoleUserEntity::getUserId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long uid : toInsert) {
                RoleUserEntity ref = new RoleUserEntity();
                ref.setRoleId(roleId);
                ref.setUserId(uid);
                this.roleUserMapper.insert(ref);
            }
        }

        incrementPermVer(affectedUsers);
    }

    // ------------------------------------------------------------------
    // 私有辅助方法
    // ------------------------------------------------------------------

    /**
     * 查询指定角色集合下所有用户的 id
     */
    private Set<Long> fetchUsersByRoleIds(Set<Long> roleIds) {
        if (CollUtils.isEmpty(roleIds)) {
            return new HashSet<>();
        }
        LambdaQueryWrapper<RoleUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RoleUserEntity::getRoleId, roleIds);
        wrapper.select(RoleUserEntity::getUserId);
        return this.roleUserMapper.selectObjs(wrapper)
                .stream()
                .map(o -> (Long) o)
                .collect(Collectors.toSet());
    }

    /**
     * 自增一批用户的权限版本号
     */
    private void incrementPermVer(Set<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            this.userMapper.incrementPermVer(userId);
        }
    }
}
