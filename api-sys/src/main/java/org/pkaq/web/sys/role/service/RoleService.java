package org.pkaq.web.sys.role.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import org.pkaq.core.annotation.BizLog;
import org.pkaq.core.enums.LockEnumm;
import org.pkaq.core.enums.StatusEnumm;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.core.mvc.util.Page;
import org.pkaq.web.sys.module.entity.ModuleEntity;
import org.pkaq.web.sys.module.mapper.ModuleMapper;
import org.pkaq.web.sys.role.entity.RoleEntity;
import org.pkaq.web.sys.role.entity.RoleModuleEntity;
import org.pkaq.web.sys.role.mapper.RoleMapper;
import org.pkaq.web.sys.role.mapper.RoleModuleMapper;
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
    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private RoleModuleMapper roleModuleMapper;
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
        Wrapper<RoleEntity> wrapper = new EntityWrapper<>();
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
        this.merge(role);
        return this.listRole(null, 1);
    }

    /**
     * 校验编码是否唯一
     * @param role
     * @return
     */
    public boolean checkUnique(RoleEntity role) {
        Wrapper<RoleEntity> entityWrapper = new EntityWrapper<>();
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
        EntityWrapper<RoleModuleEntity> wrapper = new EntityWrapper<>();
        wrapper.setEntity(roleModule);
        List<RoleModuleEntity> roleModuleList = this.roleModuleMapper.selectList(wrapper);

        Map<String, Object> map = new HashMap<>(2);
        map.put("modules", moduleList);
        map.put("roleModules", roleModuleList);
        return map;
    }
}
