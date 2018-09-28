package org.pkaq.web.sys.role.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.pkaq.core.bizlog.annotation.BizLog;
import org.pkaq.core.enums.LockEnumm;
import org.pkaq.core.enums.StatusEnumm;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.core.mvc.util.Page;
import org.pkaq.web.sys.module.entity.ModuleEntity;
import org.pkaq.web.sys.module.mapper.ModuleMapper;
import org.pkaq.web.sys.role.entity.RoleEntity;
import org.pkaq.web.sys.role.entity.RoleModuleEntity;
import org.pkaq.web.sys.role.entity.RoleUserEntity;
import org.pkaq.web.sys.role.mapper.RoleMapper;
import org.pkaq.web.sys.role.mapper.RoleModuleMapper;
import org.pkaq.web.sys.role.mapper.RoleUserMapper;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:28
 */
@Service
public class RoleService extends BaseService<RoleMapper, RoleEntity> {
    /** 权限前缀 **/
    private final static String AUTH_PREFIX = "ROLE_";

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private RoleModuleMapper roleModuleMapper;

    @Autowired
    private RoleUserMapper roleUserMapper;

    @Autowired
    private UserService userService;
    /**
     * 查询角色列表
     * @param roleEntity
     * @return
     */
    @BizLog(description = "角色新增")
    public Page<RoleEntity> listRole(RoleEntity roleEntity, Integer page) {
        return this.listPage(roleEntity, page);
    }

    /**
     * 批量删除角色
     * @param ids
     */
    public void deleteRole(ArrayList<String> ids) {
        this.mapper.deleteBatchIds(ids);
    }

    /**
     * 解锁/锁定角色
     * @param ids
     * @param lock
     */
    public void updateRole(ArrayList<String> ids, String lock) {
        RoleEntity role = new RoleEntity();
        role.setLocked(LockEnumm.LOCK.getIndex().equals(lock));
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", CollectionUtil.join(ids,","));

        this.mapper.update(role, wrapper);
    }

    /**
     * 获取一条角色信息
     * @param id 角色id
     * @return 符合条件的角色对象
     */
    public RoleEntity getRole(String id) {
        return this.mapper.selectById(id);
    }

    /**
     * 新增/编辑角色信息
     * @param role 角色对象
     * @return 角色列表
     */
    public Page<RoleEntity> saveRole(RoleEntity role) {
        // 添加 ROLE_ 前缀 并转大写
        if(!role.getCode().startsWith(AUTH_PREFIX)){
            role.setCode((AUTH_PREFIX+role.getCode()).toUpperCase());
        }
        this.merge(role);
        return this.listRole(null, 1);
    }

    /**
     * 校验编码是否唯一
     * @param role
     * @return
     */
    public boolean checkUnique(RoleEntity role) {
        QueryWrapper<RoleEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("code", role.getCode());
        int records = this.mapper.selectCount(entityWrapper);
        return records>0;
    }

    /**
     * 获取该角色绑定的所有模块
     * @param roleModule 权限条件
     * @return
     */
    public Map<String, Object> listModule(RoleModuleEntity roleModule) {
        // 获取所有菜单
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setStatus(StatusEnumm.ENABLE.getIndex());
        List<ModuleEntity> moduleList = this.moduleMapper.listModule(null, moduleEntity);
        // 获取已选的模块
        QueryWrapper<RoleModuleEntity> wrapper = new QueryWrapper<>();
        wrapper.setEntity(roleModule);
        // 只返回moduleId
        List<RoleModuleEntity> roleModuleList = this.roleModuleMapper.selectList(wrapper);
        List<String> checked = null;
        if (CollectionUtils.isNotEmpty(roleModuleList)){
            checked = new ArrayList<>(roleModuleList.size());
            for (RoleModuleEntity rme : roleModuleList) {
                checked.add(rme.getModuleId());
            }
        }

        Map<String, Object> map = new HashMap<>(2);
        map.put("modules", moduleList);
        map.put("checked", checked);
        return map;
    }

    /**
     * 保存角色关系表
     */
    public void saveModule(RoleEntity role) {
        QueryWrapper<RoleModuleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", role.getId());
        // 删除原有角色
        this.roleModuleMapper.delete(wrapper);
        // 插入新的权限信息
        if(CollectionUtil.isNotEmpty(role.getModules())){
            List<RoleModuleEntity> modules = role.getModules();
            for (RoleModuleEntity module : modules) {
                module.setRoleId(role.getId());
                this.roleModuleMapper.insert(module);
            }
        }
    }

    /**
     * 获取该角色绑定的所有用户
     * @param roleUser 权限条件
     * @return
     */
    public Map<String, Object> listUser(RoleUserEntity roleUser, Integer page) {
        // 获取所有用户
        UserEntity userEntity = new UserEntity();
        userEntity.setLocked(false);

        Page<UserEntity> pager = this.userService.listUser(userEntity, page);
        // 获取已选的模块
        QueryWrapper<RoleUserEntity> wrapper = new QueryWrapper<>();
        wrapper.setEntity(roleUser);
        // 只返回moduleId
        List<RoleUserEntity> roleUserList = this.roleUserMapper.selectList(wrapper);
        List<String> checked = null;
        if (CollectionUtils.isNotEmpty(roleUserList)){
            checked = new ArrayList<>(roleUserList.size());
            for (RoleUserEntity rue : roleUserList) {
                checked.add(rue.getUserId());
            }
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("users", pager);
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
        if(CollectionUtil.isNotEmpty(role.getUsers())){
            List<RoleUserEntity> users = role.getUsers();
            for (RoleUserEntity user : users) {
                user.setRoleId(role.getId());
                this.roleUserMapper.insert(user);
            }
        }
    }
}
