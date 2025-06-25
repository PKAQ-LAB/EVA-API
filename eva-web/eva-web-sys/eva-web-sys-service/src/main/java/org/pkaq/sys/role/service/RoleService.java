package org.pkaq.sys.role.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mybatis.enums.FrozenEnumm;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleModuleRefBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.bo.RoleUserAoeBo;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleModuleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleModuleMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.vo.RoleDetailVo;
import org.pkaq.sys.role.vo.RoleListVo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.service.UserService;
import org.pkaq.sys.user.vo.UserListVo;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class RoleService extends StdService<RoleMapper, RoleEntity, RoleConvert> {
    /**
     * 权限前缀
     **/
    private final static String AUTH_PREFIX = "ROLE_";

    private final ModuleMapper moduleMapper;

    private final RoleModuleMapper roleModuleMapper;

    private final RoleUserMapper roleUserMapper;

    private final UserService userService;

    private final RoleConvert roleConvert;

    /**
     * 查询角色列表
     */
    public List<RoleListVo> listRole(RoleQueryBo queryBo) {
        // 查询条件
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>(roleConvert.queryBoToEntity(queryBo));
        // 分页条件
        return this.roleConvert.listToVoList(this.mapper.selectList(wrapper));
    }

    /**
     * 查询角色列表
     */
    public IPage<RoleListVo> listRole(RoleQueryBo queryBo, Integer page, Integer pageSize) {

        page = null != page ? page : 1;
        pageSize = null != pageSize ? pageSize : 30;
        // 查询条件

        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>(roleConvert.queryBoToEntity(queryBo));

        // 分页条件
        PageResult<RoleEntity> pagination = new PageResult<>(page , pageSize);

        return this.mapper.selectPage(pagination, wrapper).convert(roleConvert::entityToListVo);
    }

    /**
     * 根据请求的URL查询角色所属权限
     */
    public List<Map<String, String>> listRoleNamesWithPath() {
        return this.roleModuleMapper.listRoleNamesWithPath();
    }

    /**
     * 批量删除角色
     */
    public void deleteRole(ArrayList<String> ids) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", ids);
        // 删除角色相关的 授权用户
        this.roleUserMapper.delete(queryWrapper);
        // 删除角色相关的 授权模块
        this.roleModuleMapper.delete(queryWrapper);
        //删除角色
        this.mapper.deleteBatchIds(ids);
    }

    /**
     * 解锁/锁定角色
     *
     * @param ids
     * @param lock
     */
    public void updateRole(ArrayList<String> ids, String lock) {
        RoleEntity role = new RoleEntity();
//        role.setFrozen(lock);
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", CollectionUtil.join(ids, ","));

        this.mapper.update(role, wrapper);
    }

    /**
     * 获取一条角色信息
     *
     * @param id 角色id
     * @return 符合条件的角色对象
     */
    public RoleDetailVo getRole(String id) {
        return this.roleConvert.entityToDetailVo(this.mapper.selectById(id)) ;
    }

    /**
     * 新增/编辑角色信息
     *
     * @param role 角色对象
     * @return 角色列表
     */
    public void saveRole(RoleAoeBo role) {
        // 添加 ROLE_ 前缀 并转大写
        if (!role.getCode().startsWith(AUTH_PREFIX)) {
            role.setCode((AUTH_PREFIX + role.getCode()).toUpperCase());
        }
        this.merge(role);
    }

    /**
     * 校验编码是否唯一
     *
     * @param role
     * @return
     */
    public boolean checkUnique(RoleAoeBo role) {
        // 添加 ROLE_ 前缀 并转大写
        if (!role.getCode().startsWith(AUTH_PREFIX)) {
            role.setCode((AUTH_PREFIX + role.getCode()).toUpperCase());
        }

        var entityWrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getCode, role.getCode())
                .ne(RoleEntity::getId, role.getId());

        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 获取该角色绑定的所有模块
     *
     * @param roleModule 权限条件
     * @return
     */
    public Map<String, Object> listModule(RoleModuleRefBo roleModule) {

        boolean isAdmin = ThreadUserHelper.isAdmin();
        // 获取所有菜单
        ModuleEntity moduleEntity = new ModuleEntity();
//        moduleEntity.setStatus(FrozenEnumm.UN_FROZEN.getCode());
        List<ModuleEntity> moduleList = null;

        // 非管理员仅能授权当前权限范围内的模块
        if (isAdmin) {
//            moduleList = this.moduleMapper.listModule(moduleEntity);
        } else {
            moduleList = this.moduleMapper.listGrantedModule(null, moduleEntity, new String[]{"获取角色列表"});
        }

        //获取已选且是叶子节点的模块
        List<RoleModuleEntity> roleModuleList = this.roleModuleMapper.roleModuleList(roleModule);
        // 已选的moduleId
        HashSet<String> checked = null;
        // 已选的资源权限
        Map<String, List<String>> checkedResource = new HashMap<>(roleModuleList.size());

        if (CollectionUtils.isNotEmpty(roleModuleList)) {
            checked = new HashSet<>(roleModuleList.size());
            for (RoleModuleEntity rme : roleModuleList) {
                if (!checked.contains(rme.getModuleId())) {
                    checked.add(rme.getModuleId());
                }

                String rid = rme.getResourceId();
                String mid = rme.getModuleId();

                List<String> list = checkedResource.get(mid);
                if (CollUtil.isEmpty(list)) {
                    list = new ArrayList<>();
                }
                list.add(rid);
                checkedResource.put(rme.getModuleId(), list);
            }
        }

        Map<String, Object> map = new HashMap<>(3);
        map.put("modules", moduleList);
        map.put("checked", checked);
        map.put("checkedResource", checkedResource);
        return map;
    }

    /**
     * 保存角色关系表
     */
    public void saveModule(RoleAoeBo role) {
        this.roleModuleMapper.delete(
                new LambdaQueryWrapper<RoleModuleEntity>()
                        .eq(RoleModuleEntity::getRoleId, role.getId())
        );

        // 插入新的权限信息
        if (CollUtil.isNotEmpty(role.getModules())) {
            List<RoleModuleEntity> modules = role.getModules();
            Map<String, String[]> resourceMap = role.getResources();

            for (RoleModuleEntity module : modules) {
                module.setRoleId(role.getId());
                //设置角色拥有的资源
                String[] resources = null != resourceMap ? resourceMap.get(module.getModuleId()) : null;
                if (null == resources || resources.length < 1) {
                    this.roleModuleMapper.insert(module);
                } else {
                    for (String s : resources) {
                        module.setId(IdWorker.getId());
                        module.setResourceId(s);
                        this.roleModuleMapper.insert(module);
                    }
                }
            }
        }
    }

    /**
     * 获取该角色绑定的所有用户
     *
     * @param roleId 权限条件
     * @return
     */
    public Map<String, Object> listUser(Long roleId, Long deptId) {
        // 获取所有用户
        UserQueryBo userEntity = new UserQueryBo();
        if (null != deptId && 0 != deptId) {
            userEntity.setDeptId(deptId);
        }

        userEntity.setFrozen(FrozenEnumm.UN_FROZEN.getCode());

        List<UserListVo> users = this.userService.listUser(userEntity);
        // 获取已选的模块
        RoleUserEntity roleUserEntity = new RoleUserEntity();
        roleUserEntity.setRoleId(roleId);

        QueryWrapper<RoleUserEntity> wrapper = new QueryWrapper<>();
        wrapper.setEntity(roleUserEntity);
        // 只返回moduleId
        List<RoleUserEntity> roleUserList = this.roleUserMapper.selectList(wrapper);
        List<Long> checked = null;
        if (CollectionUtils.isNotEmpty(roleUserList)) {
            checked = new ArrayList<>(roleUserList.size());
            for (RoleUserEntity rue : roleUserList) {
                checked.add(rue.getUserId());
            }
        }
        Map<String, Object> map = HashMap.newHashMap(2);
        map.put("users", users);
        map.put("checked", checked);
        return map;
    }

    /**
     * 保存角色关系表
     */
    public void saveUser(RoleUserAoeBo role) {
        // 删除原有角色
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().eq(RoleUserEntity::getRoleId, role.getRoleId()));
        // 插入新的权限信息
        if (CollUtil.isNotEmpty(role.getUsers())) {
            List<RoleUserEntity> users = role.getUsers();
            for (RoleUserEntity user : users) {
                user.setRoleId(role.getRoleId());
                this.roleUserMapper.insert(user);
            }
        }
    }

    @Override
    protected Convert<RoleEntity> getConvert() {
        return this.roleConvert;
    }
}
