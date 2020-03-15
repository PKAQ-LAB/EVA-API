package io.nerv.web.sys.role.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.enums.LockEnumm;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.core.mvc.util.EvaPage;
import io.nerv.core.util.SecurityHelper;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.mapper.ModuleMapper;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import io.nerv.web.sys.role.entity.RoleUserEntity;
import io.nerv.web.sys.role.mapper.*;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:28
 */
@Service
public class RoleService extends StdBaseService<RoleMapper, RoleEntity> {
    /** 权限前缀 **/
    private final static String AUTH_PREFIX = "ROLE_";

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private RoleModuleMapper roleModuleMapper;

    @Autowired
    private RoleUserMapper roleUserMapper;

    @Autowired
    private RoleConfigMapper roleConfigMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityHelper securityHelper;
    /**
     * 查询角色列表
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
     * @param roleEntity
     * @return
     */
    public EvaPage<RoleEntity> listRole(RoleEntity roleEntity, Integer page, Integer pageSize) {

        page = null != page ? page : 1;
        pageSize = null != pageSize? pageSize: 10;
        // 查询条件
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>(roleEntity);

        // 分页条件
        EvaPage pagination = new EvaPage();
        pagination.setCurrent(page);
        pagination.setSize(pageSize);

        return this.mapper.selectPage(pagination,wrapper);
    }
    /**
     * 根据请求的URL查询角色所属权限
     * @return
     */
    public List<Map<String, String>> listRoleNamesWithPath(){
        return this.roleModuleMapper.listRoleNamesWithPath();
    }

    /**
     * 批量删除角色
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
     * @param ids
     * @param lock
     */
    public void updateRole(ArrayList<String> ids, String lock) {
        RoleEntity role = new RoleEntity();
        role.setLocked(lock);
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
            role.setCreateBy(securityHelper.getJwtUserId());
        }
        role.setModifyBy(securityHelper.getJwtUserId());

        this.merge(role);
        return this.listRole(null, 1, 10);
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

        boolean isAdmin = securityHelper.isAdmin();

        // 获取所有菜单
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setStatus(LockEnumm.UNLOCK.getIndex());
        List<ModuleEntity> moduleList = null;

        // 非管理员仅能授权当前权限范围内的模块
        if (isAdmin){
            moduleList = this.moduleMapper.listModule(moduleEntity);
        } else {
            moduleList = this.moduleMapper.listGrantedModule(null, moduleEntity, securityHelper.getRoleNames());
        }

        //获取已选且是叶子节点的模块
        List<RoleModuleEntity> roleModuleList = this.roleModuleMapper.roleModuleList(roleModule);
        // 已选的moduleId
        HashSet<String> checked = null;
        // 已选的资源权限
        Map<String, List<String>> checkedResource = new HashMap<>(roleModuleList.size());

        if (CollectionUtils.isNotEmpty(roleModuleList)){
            checked = new HashSet<>(roleModuleList.size());
            for (RoleModuleEntity rme : roleModuleList) {
                if (!checked.contains(rme.getModuleId())){
                    checked.add(rme.getModuleId());
                }

                String rid = rme.getResourceId();
                String mid = rme.getModuleId();

                List<String> list = checkedResource.get(mid);
                if (CollUtil.isEmpty(list)){
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
        if(CollectionUtil.isNotEmpty(role.getModules())){
            List<RoleModuleEntity> modules = role.getModules();
            Map<String, String[]> resourceMap = role.getResources();

            for (RoleModuleEntity module : modules) {
                module.setRoleId(role.getId());
                //设置角色拥有的资源
                String[] resources = null != resourceMap? resourceMap.get(module.getModuleId()+"") : null;
                if (null == resources || resources.length < 1){
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
     * @param roleId 权限条件
     * @return
     */
    public Map<String, Object> listUser(String roleId, String deptId) {
        // 获取所有用户
        UserEntity userEntity = new UserEntity();
        if (StrUtil.isNotBlank(deptId)){
            userEntity.setDeptId(deptId);
        }

        userEntity.setLocked(LockEnumm.UNLOCK.getIndex());

        List<UserEntity> users  = this.userService.listUser(userEntity);
        // 获取已选的模块
        RoleUserEntity roleUserEntity = new RoleUserEntity();
        roleUserEntity.setRoleId(roleId);

        QueryWrapper<RoleUserEntity> wrapper = new QueryWrapper<>();
        wrapper.setEntity(roleUserEntity);
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
        if(CollectionUtil.isNotEmpty(role.getUsers())){
            List<RoleUserEntity> users = role.getUsers();
            for (RoleUserEntity user : users) {
                user.setRoleId(role.getId());
                this.roleUserMapper.insert(user);
            }
        }
    }
}
