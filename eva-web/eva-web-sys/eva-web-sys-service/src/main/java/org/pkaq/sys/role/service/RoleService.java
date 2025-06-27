package org.pkaq.sys.role.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.convert.RoleConvert;
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
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class RoleService extends StdService<RoleMapper, RoleEntity, RoleConvert> implements IRoleService {

    private final RoleResourceMapper roleResourceMapper;

    private final RoleUserMapper roleUserMapper;

    private final ModuleMapper moduleMapper;

    private final UserMapper userMapper;

    private final UserConvert userConvert;

    /**
     * 根据请求的URL查询角色所属权限
     */
    @Override
    public List<Map<String, String>> listRoleNamesWithPath() {
        return this.roleResourceMapper.listRoleNamesWithPath();
    }

    /**
     * 批量删除角色
     */
    @Override
    public void delete(List<Long> ids) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", ids);
        // 删除该角色 授权的用户(可能有其它角色绑定)
        this.roleUserMapper.delete(queryWrapper);
        // 删除该角色 授权的模块以及资源
        this.roleResourceMapper.delete(queryWrapper);
        //删除角色
        this.mapper.deleteByIds(ids);
    }

    /**
     * 校验编码是否唯一
     *
     * @param idCodeBo
     * @return
     */
    @Override
    public boolean isUnique(IdCodeBo idCodeBo) {
        // 添加 ROLE_ 前缀 并转大写
        if (!idCodeBo.getCode().startsWith(CommonConstant.AUTH_PREFIX)) {
            idCodeBo.setCode((CommonConstant.AUTH_PREFIX + idCodeBo.getCode()).toUpperCase());
        }

        var entityWrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getCode, idCodeBo.getCode())
                .ne(RoleEntity::getId, idCodeBo.getId());

        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 获取该角色绑定的所有模块 资源
     *
     * @param roleModule 权限条件
     * @return
     */
    @Override
    public RoleGrantedModuleVo fetchResource(RoleResourceRefBo roleModule) {
        if (null == roleModule || roleModule.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
        }
        // 获取角色范围内的不重复菜单
        var curUid = ThreadUserHelper.getUserId();
        var roleId = roleModule.getRoleId();

        Map<Long, ModuleDetailVo> moduleMap = this.moduleMapper.listGrantedModules(curUid);

        // 查询该角色拥有的所有资源
        var resourceMap = this.roleResourceMapper.listGrantedResource(roleId);

        var moduleChecked = new HashSet<Long>();
        // 将资源组装到模块中
        resourceMap.forEach((k, v) -> {
            var module = moduleMap.get(k);
            module.setResources(v);
            // 收集模块的选中id
            moduleChecked.add(k);
        });

        RoleGrantedModuleVo roleModuleVo = new RoleGrantedModuleVo();
        roleModuleVo.setModules(moduleMap.values());
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
        if (CollUtil.isNotEmpty(role.getResourceId())) {
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
     * 获取该角色绑定的所有用户
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

        // 获取已选的用户
        RoleUserEntity roleUserEntity = new RoleUserEntity();
        roleUserEntity.setRoleId(roleId);

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
        if (CollUtil.isNotEmpty(role.getUserId())) {
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
