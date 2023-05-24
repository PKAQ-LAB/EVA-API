package io.nerv.sys.role.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.nerv.core.enums.LockEnumm;
import io.nerv.core.mybatis.mvc.service.mybatis.StdService;
import io.nerv.core.mybatis.mvc.util.Page;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.sys.module.entity.ModuleEntityStd;
import io.nerv.sys.module.mapper.ModuleMapper;
import io.nerv.sys.role.entity.RoleEntity;
import io.nerv.sys.role.entity.RoleModuleEntity;
import io.nerv.sys.role.entity.RoleUserEntity;
import io.nerv.sys.role.mapper.RoleConfigMapper;
import io.nerv.sys.role.mapper.RoleMapper;
import io.nerv.sys.role.mapper.RoleModuleMapper;
import io.nerv.sys.role.mapper.RoleUserMapper;
import io.nerv.sys.user.entity.UserEntity;
import io.nerv.sys.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class RoleService extends StdService<RoleMapper, RoleEntity> {
    /**
     * 权限前缀
     **/
    private final static String AUTH_PREFIX = "ROLE_";

    private final ModuleMapper moduleMapper;

    private final RoleModuleMapper roleModuleMapper;

    private final RoleUserMapper roleUserMapper;

    private final RoleConfigMapper roleConfigMapper;

    private final UserService userService;

    /**
     * 查询角色列表
     *
     * @param roleEntity
     * @return
     */
    public List<RoleEntity> listRole(RoleEntity roleEntity) {
        // 查询条件
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>(roleEntity);
        // 分页条件
        return this.mapper.selectList(wrapper);
    }

    /**
     * 查询角色列表
     *
     * @param roleEntity
     * @return
     */
    public IPage<RoleEntity> listRole(RoleEntity roleEntity, Integer page, Integer pageSize) {

        page = null != page ? page : 1;
        pageSize = null != pageSize ? pageSize : 10;
        // 查询条件
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>(roleEntity);

        // 分页条件
        Page pagination = new Page();
        pagination.setCurrent(page);
        pagination.setSize(pageSize);

        return this.mapper.selectPage(pagination, wrapper);
    }

    /**
     * 根据请求的URL查询角色所属权限
     *
     * @return
     */
    public List<Map<String, String>> listRoleNamesWithPath() {
        return this.roleModuleMapper.listRoleNamesWithPath();
    }

    /**
     * 批量删除角色
     *
     * @param ids
     */
    public void deleteRole(ArrayList<String> ids) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("role_id", ids);
        // 删除角色相关的 授权用户
        this.roleUserMapper.delete(queryWrapper);
        // 删除角色相关的 授权模块
        this.roleModuleMapper.delete(queryWrapper);
        // 删除角色相关的 授权参数
        this.roleConfigMapper.delete(queryWrapper);
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
        role.setLocked(lock);
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
    public RoleEntity getRole(String id) {
        return this.mapper.selectById(id);
    }

    /**
     * 新增/编辑角色信息
     *
     * @param role 角色对象
     * @return 角色列表
     */
    public void saveRole(RoleEntity role) {
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
    public boolean checkUnique(RoleEntity role) {
        QueryWrapper<RoleEntity> entityWrapper = new QueryWrapper<>();
        // 添加 ROLE_ 前缀 并转大写
        if (!role.getCode().startsWith(AUTH_PREFIX)) {
            role.setCode((AUTH_PREFIX + role.getCode()).toUpperCase());
        }
        entityWrapper.eq("code", role.getCode());
        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 获取该角色绑定的所有模块
     *
     * @param roleModule 权限条件
     * @return
     */
    public Map<String, Object> listModule(RoleModuleEntity roleModule) {

        boolean isAdmin = ThreadUserHelper.isAdmin();
        // 获取所有菜单
        ModuleEntityStd moduleEntity = new ModuleEntityStd();
        moduleEntity.setStatus(LockEnumm.UNLOCK.getCode());
        List<ModuleEntityStd> moduleList = null;

        // 非管理员仅能授权当前权限范围内的模块
        if (isAdmin) {
            moduleList = this.moduleMapper.listModule(moduleEntity);
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
    public void saveModule(RoleEntity role) {
        QueryWrapper<RoleModuleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", role.getId());
        // 删除角色原有的模块
        this.roleModuleMapper.delete(wrapper);

        // 插入新的权限信息
        if (CollectionUtil.isNotEmpty(role.getModules())) {
            List<RoleModuleEntity> modules = role.getModules();
            Map<String, String[]> resourceMap = role.getResources();

            for (RoleModuleEntity module : modules) {
                module.setRoleId(role.getId());
                //设置角色拥有的资源
                String[] resources = null != resourceMap ? resourceMap.get(module.getModuleId() + "") : null;
                if (null == resources || resources.length < 1) {
                    this.roleModuleMapper.insert(module);
                } else {
                    for (String s : resources) {
                        module.setId(IdWorker.getIdStr());
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
    public Map<String, Object> listUser(String roleId, String deptId) {
        // 获取所有用户
        UserEntity userEntity = new UserEntity();
        if (StrUtil.isNotBlank(deptId)) {
            userEntity.setDeptId(deptId);
        }

        userEntity.setLocked(LockEnumm.UNLOCK.getCode());

        List<UserEntity> users = this.userService.listUser(userEntity);
        // 获取已选的模块
        RoleUserEntity roleUserEntity = new RoleUserEntity();
        roleUserEntity.setRoleId(roleId);

        QueryWrapper<RoleUserEntity> wrapper = new QueryWrapper<>();
        wrapper.setEntity(roleUserEntity);
        // 只返回moduleId
        List<RoleUserEntity> roleUserList = this.roleUserMapper.selectList(wrapper);
        List<String> checked = null;
        if (CollectionUtils.isNotEmpty(roleUserList)) {
            checked = new ArrayList<>(roleUserList.size());
            for (RoleUserEntity rue : roleUserList) {
                checked.add(rue.getUserId());
            }
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("users", users);
        map.put("checked", checked);
        return map;
    }

    /**
     * 保存角色关系表
     */
    public void saveUser(RoleEntity role) {
        QueryWrapper<RoleUserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", role.getId());
        // 删除原有角色
        this.roleUserMapper.delete(wrapper);
        // 插入新的权限信息
        if (CollectionUtil.isNotEmpty(role.getUsers())) {
            List<RoleUserEntity> users = role.getUsers();
            for (RoleUserEntity user : users) {
                user.setRoleId(role.getId());
                this.roleUserMapper.insert(user);
            }
        }
    }
}
