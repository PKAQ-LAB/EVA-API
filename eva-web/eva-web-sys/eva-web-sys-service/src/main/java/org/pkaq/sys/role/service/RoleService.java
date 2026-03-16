package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
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
     * 批量删除角色
     */
    @Override
    public void delete(Set<Long> ids) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", ids);
        // 删除角色授权的用户，可能有其他角色绑定
        this.roleUserMapper.delete(queryWrapper);
        // 删除角色授权的模块及资源
        this.roleResourceMapper.delete(queryWrapper);
        // 删除角色
        this.mapper.deleteByIds(ids);
    }

    /**
     * 校验编码是否唯一
     */
    @Override
    public boolean isUnique(IdCodeBo idCodeBo) {
        // 添加 ROLE_ 前缀并转大写
        if (!idCodeBo.getCode().startsWith(CommonConstant.AUTH_PREFIX)) {
            idCodeBo.setCode((CommonConstant.AUTH_PREFIX + idCodeBo.getCode()).toUpperCase());
        }

        var entityWrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getCode, idCodeBo.getCode())
                .ne(idCodeBo.getId() != null, RoleEntity::getId, idCodeBo.getId());

        return this.mapper.selectCount(entityWrapper) > 0;
    }

    /**
     * 获取角色绑定的所有模块资源
     *
     * @param roleModule 权限条件
     */
    @Override
    public RoleGrantedModuleVo fetchResource(RoleResourceRefBo roleModule) {
        if (null == roleModule || roleModule.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
        }
        // 获取角色范围内的模块
        var curUid = ThreadUserHelper.getUserId();
        var roleId = roleModule.getRoleId();

        Map<Long, ModuleDetailVo> moduleMap = this.moduleMapper.listGrantedModules(curUid);

        // 查询角色拥有的资源
        var resourceMap = this.roleResourceMapper.listGrantedResource(roleId);

        var moduleChecked = new HashSet<Long>();
        // 将资源组装到模块中
        resourceMap.forEach((k, v) -> {
            var module = moduleMap.get(k);
            module.setResources(v);
            // 收集模块选中id
            moduleChecked.add(k);
        });

        // 菜单转换为树
        var moduleTree = TreeHelper.buildTree(moduleMap.values());

        RoleGrantedModuleVo roleModuleVo = new RoleGrantedModuleVo();
        roleModuleVo.setModules(moduleTree);
        roleModuleVo.setCheckedModuleIds(moduleChecked);

        return roleModuleVo;
    }

    /**
     * 保存角色关系表
     */
    @Override
    public void grantResource(RoleResourceRefBo role) {
        if (null == role.getRoleId()) {
            CommonCodes.PARAM_ERROR.newException();
        }
        this.roleResourceMapper.delete(
                new LambdaQueryWrapper<RoleResourceEntity>()
                        .eq(RoleResourceEntity::getRoleId, role.getRoleId())
        );

        // 写入资源信息
        if (CollUtils.isNotEmpty(role.getResourceId())) {
            List<Long> resources = role.getResourceId();

            for (Long rid : resources) {
                var ref = new RoleResourceEntity();
                ref.setRoleId(role.getRoleId());
                ref.setResourceId(rid);

                this.roleResourceMapper.insert(ref);
            }
        }
    }

    /**
     * 获取角色绑定的所有用户
     *
     * @param roleId 权限条件
     * @return
     */
    @Override
    public RoleGrantedUserVo listUser(Long roleId, Long deptId) {
        // 获取所有用户
        LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq((null != deptId && 0 != deptId), UserEntity::getDeptId, deptId);
        userWrapper.eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN);

        List<UserEntity> users = this.userMapper.selectList(userWrapper);

        // 获取已选用户
        LambdaQueryWrapper<RoleUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleUserEntity::getRoleId, roleId);
        wrapper.select(RoleUserEntity::getUserId);

        Set<Long> checkedUser = this.roleUserMapper.selectObjs(wrapper)
                .stream()
                .map(o -> (Long) o)
                .collect(Collectors.toSet());

        RoleGrantedUserVo roleGrantedUserVo = new RoleGrantedUserVo();
        roleGrantedUserVo.setCheckedUser(checkedUser);
        roleGrantedUserVo.setUsers(this.userConvert.entityToSimpleVo(users));

        return roleGrantedUserVo;
    }

    /**
     * 保存角色关系表
     */
    @Override
    public void grantUser(RoleUserRefBo role) {
        if (null == role.getRoleId()) {
            CommonCodes.PARAM_ERROR.newException();
        }
        // 删除原有角色
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().eq(RoleUserEntity::getRoleId, role.getRoleId()));
        // 插入新的权限信息
        if (CollUtils.isNotEmpty(role.getUserId())) {
            List<Long> users = role.getUserId();
            for (Long user : users) {
                var ref = new RoleUserEntity();
                ref.setRoleId(role.getRoleId());
                ref.setUserId(user);
                this.roleUserMapper.insert(ref);
            }
        }
    }
}
