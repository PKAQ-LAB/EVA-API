package org.pkaq.sys.tenant.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogEnum;
import org.pkaq.core.mybatis.enums.FrozenEnumm;
import org.pkaq.core.mybatis.util.Page;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.tenant.bo.TenantAuthBo;
import org.pkaq.sys.tenant.bo.TenantEditBo;
import org.pkaq.sys.tenant.bo.TenantQueryBo;
import org.pkaq.sys.tenant.bo.TenantStatusBo;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.entity.TenantRoleEntity;
import org.pkaq.sys.tenant.mapper.TenantMapper;
import org.pkaq.sys.tenant.mapper.TenantRoleMapper;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.tenant.vo.TenantLeftListVo;
import org.pkaq.sys.tenant.vo.TenantListVo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * 租户管理Service
 *
 * @author PKAQ
 */
@Service
@Schema(description = "租户管理")
@AllArgsConstructor
public class TenantService {
    private final TenantMapper mapper;
    private final UserMapper userMapper;
    private final TenantRoleMapper tenantRoleMapper;
    private final RoleUserMapper roleUserMapperl;
    private final UserService userService;

    private final TenantConvert tenantConvert;

    /**
     * 根据ID批量删除
     */
    @BizLog(operateType = BizLogEnum.DELETE, description = "删除租户[{0}]", args = {"param:0"})
    @Transactional
    public void delete(List<String> ids) {
        // 删除租户
        if (CollUtil.isNotEmpty(ids)) {
            this.mapper.deleteByIds(ids);
            // 删除租户所有用户 并踢出去
            List<String> userIds = this.userMapper.selectObjs(Wrappers.<UserEntity>lambdaQuery().select(UserEntity::getId).in(UserEntity::getTenantId, ids));
            userService.delete((ArrayList<String>) userIds);
        }
    }

    /**
     * 新增/编辑一条租户信息
     */
    @BizLog(operateType = BizLogEnum.EDIT, description = "编辑租户[{0}]", args = {"param:0.id"})
    @Transactional
    public void edit(TenantEditBo editBo) {
        boolean isNew = CharSequenceUtil.isBlank(editBo.getId());
        String tid = isNew ? IdWorker.getIdStr() : editBo.getId();
        editBo.setId(tid);

        TenantEntity entity = tenantConvert.boToEntity(editBo);
        if (isNew) {
            var adminId = IdWorker.getIdStr();
            entity.setAdminId(adminId);
            this.mapper.insert(entity);

            // 初始化管理员用户
            insertUser(editBo, adminId);
            // 使用json初始化租户数据
            initJson(editBo, adminId);
        } else {
            // 租户号只读 不能修改
            entity.setCode(null);
            this.mapper.updateById(entity);
            // 锁定超出数量的用户
            this.mapper.lockExcessUsers(tid, editBo.getAuthUserCount());
        }
    }

    /**
     * 根据ID获取一条租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据id查询租户")
    public TenantDetailVo get(String id) {
        var entity = this.mapper.selectById(id);
        var vo = tenantConvert.entityToVo(entity);

        // 查询管理员账号
        Optional.ofNullable(userMapper.selectById(entity.getAdminId())).ifPresent(admin -> vo.setAdminAccount(admin.getAccount()));
        return vo;
    }

    /**
     * 根据属性查询租户列表
     *
     * @return 租户列表
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "查询租户")
    public IPage<TenantListVo> list(TenantQueryBo queryBo) {
        // 获取分页数据  模糊查询
        Page<TenantListVo> pagination = new Page<>(queryBo.getPageNo(), queryBo.getPageSize());
        return this.mapper.listPage(pagination, queryBo);
    }

    /**
     * 切换可用状态
     */
    @BizLog(operateType = BizLogEnum.UPDATE, description = "切换租户[{0}]状态", args = {"param:0.id"})
    @Transactional
    public void switchStatus(TenantStatusBo bo) {
        var entity = tenantConvert.boToEntity(bo);
        this.mapper.updateById(entity);
    }

    /**
     * 校验code/名称是否唯一
     */
    public boolean checkUnique(TenantEditBo editBo) {
        var wrapper = Wrappers.<TenantEntity>lambdaQuery()
                .eq(CharSequenceUtil.isNotBlank(editBo.getCode()), TenantEntity::getCode, editBo.getCode())
                .eq(CharSequenceUtil.isNotBlank(editBo.getName()), TenantEntity::getName, editBo.getName())
                .ne(CharSequenceUtil.isNotBlank(editBo.getId()), TenantEntity::getId, editBo.getId());
        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 插入用户
     */
    private void insertUser(TenantEditBo editBo, String uId) {
        // 插入用户
        UserEntity user = new UserEntity();
        user.setId(uId);
        user.setDeptId(editBo.getId());
        user.setFrozen("9999");
        user.setAccount(editBo.getAdminAccount());
        user.setPassword(BCrypt.hashpw(editBo.getAdminPass()));
        user.setTenantId(editBo.getId());
        userMapper.insert(user);
    }

    /**
     * 保存租户角色关系
     *
     * @param authBo
     */
    @BizLog(operateType = BizLogEnum.UPDATE, description = "保存租户角色关系")
    @Transactional
    public void saveAuth(TenantAuthBo authBo) {
        Optional.ofNullable(authBo).ifPresent(bo -> {
            var tId = bo.getId();
            var rId = bo.getRoleId();
            // 租户记录
            TenantEntity tenant = this.mapper.selectById(bo.getId());

            var wrapper = Wrappers.<TenantRoleEntity>lambdaQuery().eq(TenantRoleEntity::getTenantId, tId);
            TenantRoleEntity tr = new TenantRoleEntity();
            tr.setTenantId(tId);
            tr.setRoleId(rId);
            var isUpdate = tenantRoleMapper.update(tr, wrapper) > 0;

            RoleUserEntity roleUser = new RoleUserEntity();
            roleUser.setRoleId(rId);
            roleUser.setUserId(tenant.getAdminId());
            if (isUpdate) {
                // 修改租户管理员的角色
                roleUserMapperl.update(roleUser, new QueryWrapper<RoleUserEntity>().eq("user_id", roleUser.getUserId()).eq("role_id", bo.getOldRole()));
                // 根据角色id和租户id 删除该租户创建的角色中多余的模块
                tenantRoleMapper.removeRedundantModulesForTenant(authBo.getId(), authBo.getRoleId());
            } else {
                // 插入租户角色关系表
                tenantRoleMapper.insert(tr);
                // 插入管理员用户角色关系表
                roleUserMapperl.insert(roleUser);
            }
        });
    }

    /**
     * 角色/用户/部门/岗位管理左侧租户列表
     */
    public List<TenantLeftListVo> listNoPage() {
        var list = this.mapper.selectList(Wrappers.<TenantEntity>lambdaQuery().eq(TenantEntity::getFrozen, FrozenEnumm.UN_FROZEN.getCode()));
        return JsonUtil.parseArray(JsonUtil.toJson(list), TenantLeftListVo.class);
    }

    @Data
    static class InitData {
        private String tableName;
        private List<JSONObject> records;
        private JSONObject publicFields;
    }

    /**
     * 用逗号连接所有属性名/用逗号连接所有属性值
     */
    @Data
    static class KeyValuePair {
        private StringJoiner keyJoiner;
        private StringJoiner valueJoiner;

        KeyValuePair() {
            this.keyJoiner = new StringJoiner(",");
            this.valueJoiner = new StringJoiner(",");
        }

        /**
         * 合并key和合并value
         */
        public void merge(KeyValuePair other) {
            this.keyJoiner.merge(other.getKeyJoiner());
            this.valueJoiner.merge(other.getValueJoiner());
        }

        public void keyAdd(CharSequence newElement) {
            this.keyJoiner.add(newElement);
        }

        public void valueAdd(CharSequence newElement) {
            this.valueJoiner.add(newElement);
        }
    }

    /**
     * 使用json初始化租户数据
     */
    private void initJson(TenantEditBo editBo, String uId) {
        // TODO
        // 初始化变量map
//        Map<String, String> globalMap = new HashMap<>();
//        globalMap.put(InitVariablesConstant.TENANT_ID, editBo.getId());
//        globalMap.put(InitVariablesConstant.TENANT_NAME, editBo.getFullName());
//        globalMap.put(InitVariablesConstant.CREATE_ID, uId);
//        globalMap.put(InitVariablesConstant.CREATE_BY, editBo.getName());
//        globalMap.put(InitVariablesConstant.NOW, DateUtil.date().toString());
//
//        // 使用类加载器获取位于resources/init目录下的data.json文件的输入流
//        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(InitVariablesConstant.FILE_NAME)) {
//            // 读取json数据
//            List<InitData> dataList = JsonUtil.parse(inputStream, new TypeReference<List<InitData>>() {
//            });
//            // 处理InitData
//            handleInitData(globalMap, dataList);
//        } catch (IOException | IllegalArgumentException e) {
//            // 当inputStream==null时，会抛出IllegalArgumentException
//            throw InitCode.INIT_FILE_NOT_EXIST.newException();
//        }
    }

//    private void handleInitData(Map<String, String> globalMap, List<InitData> dataList) {
//        if (CollUtil.isEmpty(globalMap) || CollUtil.isEmpty(dataList)) {
//            return;
//        }
//        dataList.forEach(item -> {
//            if (CharSequenceUtil.isBlank(item.getTableName()) || CollUtil.isEmpty(item.getRecords())) {
//                return;
//            }
//            // 处理公共属性
//            var publicJoiner = handleJsonObject(item.getPublicFields(), globalMap);
//            // 处理单独属性
//            item.getRecords().forEach(record -> {
//                // 得到并移除孩子节点
//                List<InitData> childList = null;
//                JSONArray childArray = record.getJSONArray(InitVariablesConstant.CHILD);
//                if (childArray != null) {
//                    childList = childArray.toList(InitData.class);
//                    record.remove(InitVariablesConstant.CHILD);
//                }
//
//                // 插入主数据
//                var joiner = handleJsonObject(record, globalMap);
//                joiner.merge(publicJoiner);
//                String id = insertData(item.getTableName(), joiner);
//
//                // 处理子数据
//                if (CollUtil.isNotEmpty(childList)) {
//                    // 往map中加上主表id
//                    globalMap.put(InitVariablesConstant.MAIN_ID, id);
//                    handleInitData(globalMap, childList);
//                }
//            });
//        });
//    }
//
//    private String insertData(String tableName, KeyValuePair joiner) {
//        // 手动单独处理id 直接替换$$ID$$
//        String id = IdWorker.getIdStr();
//        var valueStr = joiner.getValueJoiner().toString().replace(InitVariablesConstant.ID, id);
//        String sql = "INSERT INTO %s (%s) VALUES (%s)".formatted(tableName, joiner.getKeyJoiner(), valueStr);
//        jdbcTemplate.update(sql);
//        return id;
//    }
//
//    private KeyValuePair handleJsonObject(JSONObject jsonObject, Map<String, String> globalMap) {
//        KeyValuePair keyValueJoiner = new KeyValuePair();
//        Optional.ofNullable(jsonObject).ifPresent(i -> {
//            jsonObject.forEach((k, v) -> {
//                keyValueJoiner.keyAdd(k);
//                // value需要加上双引号
//                String quotedValue = JSONUtil.quote(globalMap.getOrDefault(v, String.valueOf(v)), true);
//                keyValueJoiner.valueAdd(quotedValue);
//            });
//        });
//        return keyValueJoiner;
//    }
}
