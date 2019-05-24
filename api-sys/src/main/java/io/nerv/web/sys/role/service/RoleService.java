package io.nerv.web.sys.role.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.enums.LockEnumm;
import io.nerv.core.enums.StatusEnumm;
import io.nerv.security.util.SecurityUtil;
import io.nerv.web.sys.user.service.UserService;
import io.nerv.core.bizlog.annotation.BizLog;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.mapper.ModuleMapper;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import io.nerv.web.sys.role.entity.RoleUserEntity;
import io.nerv.web.sys.role.mapper.RoleMapper;
import io.nerv.web.sys.role.mapper.RoleModuleMapper;
import io.nerv.web.sys.role.mapper.RoleUserMapper;
import io.nerv.web.sys.user.entity.UserEntity;
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

    @Autowired
    private SecurityUtil securityUtil;
    /**
     * 查询角色列表
     * @param roleEntity
     * @return
     */
    public IPage<RoleEntity> listRole(RoleEntity roleEntity, Integer page) {

        boolean isAdmin = securityUtil.isAdmin();
        page = null != page ? page : 1;
        // 查询条件
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>(roleEntity);
        // 非管理员只查询当前拥有权限 或 当前创建人创建的角色 列表
        if (!isAdmin) {
            wrapper.in("CREATE_BY", securityUtil.getJwtUserId());
        }

        // 分页条件
        Page pagination = new Page();
        pagination.setCurrent(page);
        return this.mapper.selectPage(pagination,wrapper);
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
    public IPage<RoleEntity> saveRole(RoleEntity role) {
        // 添加 ROLE_ 前缀 并转大写
        if(!role.getCode().startsWith(AUTH_PREFIX)){
            role.setCode((AUTH_PREFIX+role.getCode()).toUpperCase());
        }
        // 设置创建人 修改人
        if (StrUtil.isBlank(role.getId())){
            role.setCreateBy(securityUtil.getJwtUserId());
        }
        role.setModifyBy(securityUtil.getJwtUserId());

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
        // 添加 ROLE_ 前缀 并转大写
        if(!role.getCode().startsWith(AUTH_PREFIX)){
            role.setCode((AUTH_PREFIX+role.getCode()).toUpperCase());
        }
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

        boolean isAdmin = securityUtil.isAdmin();

        // 获取所有菜单
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setStatus(StatusEnumm.ENABLE.getIndex());
        List<ModuleEntity> moduleList = null;

        // 非管理员仅能授权当前权限范围内的模块
        if (isAdmin){
            moduleList = this.moduleMapper.listModule(null, moduleEntity);
        } else {
            moduleList = this.moduleMapper.listGrantedModule(null, moduleEntity, securityUtil.getRoleNames());
        }

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
        userEntity.setLocked(LockEnumm.UNLOCK.getIndex());

        IPage<UserEntity> pager = this.userService.listUser(userEntity, page);
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
