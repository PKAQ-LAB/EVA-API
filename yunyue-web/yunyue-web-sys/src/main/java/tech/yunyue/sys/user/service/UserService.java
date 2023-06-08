package tech.yunyue.sys.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import tech.yunyue.core.enums.LockEnumm;
import tech.yunyue.core.enums.OrgTypeEnum;
import tech.yunyue.core.log.annotation.BizLog;
import tech.yunyue.core.log.base.BizLogEnum;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.events.KickUserEvent;
import tech.yunyue.sys.dict.cache.DictCacheHelper;
import tech.yunyue.sys.module.entity.ModuleEntityStd;
import tech.yunyue.sys.module.mapper.ModuleMapper;
import tech.yunyue.sys.organization.mapper.OrganizationMapper;
import tech.yunyue.sys.role.entity.RoleUserEntity;
import tech.yunyue.sys.role.mapper.RoleUserMapper;
import tech.yunyue.sys.user.entity.UserEntity;
import tech.yunyue.sys.user.mapper.UserMapper;
import tech.yunyue.sys.user.vo.PasswordVO;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mybatis.mvc.service.mybatis.StdService;
import tech.yunyue.core.mybatis.mvc.util.Page;
import tech.yunyue.core.mybatis.util.tree.TreeHelper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户管理
 *
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:00
 */

@Service
@AllArgsConstructor
@Schema(description = "用户管理")
public class UserService extends StdService<UserMapper, UserEntity> {

    private final OrganizationMapper organizationMapper;

    private final RoleUserMapper roleUserMapper;

    private final FileUploadProvider fileUploadProvider;

    private final ModuleMapper moduleMapper;

    private final DictCacheHelper dictCacheHelper;

    private final ApplicationEventPublisher publisher;
    private final EvaConfig evaConfig;

    /**
     * 修改密码
     *
     * @param passwordVO
     * @return
     */
    @BizLog(operateType = BizLogEnum.UPDATE, description = "修改密码")
    public boolean repwd(PasswordVO passwordVO) {
        //TODO 获取用户ID
        UserEntity userEntity = this.mapper.selectById(passwordVO.getUserId());

        if (BCrypt.checkpw(passwordVO.getOriginpassword(), userEntity.getPassword())) {
            userEntity.setPassword(BCrypt.hashpw(passwordVO.getNewpassword()));
            this.mapper.updateById(userEntity);
			//踢出当前用户
            kickOut(Collections.singletonList(passwordVO.getUserId()));
            return true;
        }
        return false;
    }

    /**
     * 查询用户列表
     *
     * @param userEntity
     * @return
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "分页查询用户列表")
    public IPage<UserEntity> listUser(UserEntity userEntity, Integer page, Integer size) {
        page = null != page ? page : 1;
        size = null != size ? size : 10;

        Page pagination = new Page();
        pagination.setCurrent(page);
        pagination.setSize(size);

        return this.mapper.getUerWithRoleId(pagination, userEntity);
    }

    /**
     * 查询用户列表 无分页
     *
     * @param userEntity
     * @return
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "查询用户列表")
    public List<UserEntity> listUser(UserEntity userEntity) {
        return this.list(userEntity);
    }

    /**
     * 解锁/锁定用户
     *
     * @param ids
     * @param lock
     */
    @BizLog(operateType = BizLogEnum.UPDATE, description = "解锁/锁定用户")
    public void updateUser(ArrayList<String> ids, String lock) {
        UserEntity user = new UserEntity();
        user.setLocked(lock);
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);

        this.mapper.update(user, wrapper);
        //踢出被锁定的用户
        if (LockEnumm.LOCK.getCode().equals(lock)) this.kickOut(ids);
    }

    /**
     * 获取一条用户信息
     *
     * @param id 用户id
     * @return 符合条件的用户对象
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据id查询用户")
    public UserEntity getUser(String id) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        return this.mapper.getUserWithRole(userEntity);
    }

    /**
     * 新增/编辑用户信息
     *
     * @param user 用户对象
     * @return 用户列表
     */
    @BizLog(operateType = BizLogEnum.CREATE_UPDATE, description = "保存用户记录[{0}]",args = {"param:0.id"})
    public void saveUser(UserEntity user) {
        // 用户资料发生修改后 重新生成密码
        // 这里传递过来的密码是进行md5加密后的
        String pwd = user.getPassword();
        if (StrUtil.isNotBlank(pwd)) {
            pwd = BCrypt.hashpw(pwd);
            user.setPassword(pwd);
        }
        //设置部门名称与租户信息
        if (StrUtil.isNotBlank(user.getDeptId())) {
            var org = organizationMapper.selectById(user.getDeptId());
            user.setDeptName(org.getName());
            //设置用户的租户  与所属组织的一样
            user.setTenantId(org.getTenantId());
            user.setCompanyTenantId(org.getCompanyTenantId());
        }else{
            //用户不选择部门时 租户则与当前登录用户租户一致
            user.setTenantId(ThreadUserHelper.getTenantId());
            user.setCompanyTenantId(ThreadUserHelper.getComTenantId());
        }

        // 新增手工生成主键
        // 编辑， 删除原有头像文件，保存新的头像文件
        String userId = user.getId();
        boolean isInsert = true;
        if (StrUtil.isBlank(userId)) {
            userId = IdWorker.getIdStr();
            user.setId(userId);
        } else {
            isInsert = false;
            UserEntity oldUser = this.mapper.selectById(userId);
            String avatar = oldUser.getAvatar();
            if (StrUtil.isNotBlank(avatar) && !avatar.equals(user.getAvatar())) {
                fileUploadProvider.delFromStorage(avatar);
            }
            //修改用户时不能修改用户所属公司和集团
            if(!Objects.equals(oldUser.getTenantId(), user.getTenantId())
                    || !Objects.equals(oldUser.getCompanyTenantId(), user.getCompanyTenantId())){
                BizCodeEnum.NO_CHANGE_COMPANY.newException();
            }
        }

        // 保存新的头像文件
        if (StrUtil.isNotBlank(user.getAvatar())) {
            fileUploadProvider.storageWithThumbnail(0.3f, user.getAvatar());
        }
        // 保存权限
        this.saveRoles(user);

        if (isInsert) {
            this.mapper.insert(user);
        } else {
            this.mapper.updateById(user);
        }
        this.merge(user);
    }

    /**
     * 校验账号是否唯一
     *
     * @param user
     * @return
     */
    public boolean checkUnique(UserEntity user) {
        QueryWrapper<UserEntity> entityWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(user.getAccount())) {
            entityWrapper.eq("account", user.getAccount());
        }
        if (StrUtil.isNotBlank(user.getId())) {
            entityWrapper.ne("id", user.getId());
        }
        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 获取当前登录用户的信息(菜单.权限.消息
     *
     * @param uid 用户ID
     * @return
     */
    public List<ModuleEntityStd> fetch(String uid) {
        List<ModuleEntityStd> moduleEntity = this.moduleMapper.getRoleModuleByUserId(uid);
        List<ModuleEntityStd> treeModule = TreeHelper.bulid(moduleEntity);

        BizCodeEnum.PERMISSION_EXPIRED.assertNotBlank(treeModule);

        return treeModule;
    }

    /**
     * 保存用户权限
     *
     * @param user
     */
    @BizLog(operateType = BizLogEnum.UPDATE, description = "保存用户权限")
    public void saveRoles(UserEntity user) {
        // 保存权限
        if (CollUtil.isNotEmpty(user.getRoles())) {
            // 先删除该用户原有的权限
            QueryWrapper<RoleUserEntity> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("user_id", user.getId());

            this.roleUserMapper.delete(deleteWrapper);
            // 再插入更新后的权限
            user.getRoles().forEach(item -> {
                RoleUserEntity roleUserEntity = new RoleUserEntity();
                roleUserEntity.setRoleId(item.getId());
                roleUserEntity.setUserId(user.getId());
                roleUserMapper.insert(roleUserEntity);
            });
        }
    }

    /**
     * 根据用户id查询用户角色
     */
    public List<String> getRoleById(String userId) {
        return this.mapper.getRoleById(userId);
    }

    /**
     * @return 登录用户的基本信息/资源信息/参数配置/列头配置
     */
    public Map<String, Object> init() {
        Map<String,Object> reMap = new HashMap<>(4);
        var userId = ThreadUserHelper.getUserId();

        //登录用户基本信息
        reMap.put("userInfo",ThreadUserHelper.getCurrentUser());
        //登录用户模块资源信息
        reMap.put("menus",this.fetch(userId));
        //参数配置

        //列头配置
        return reMap;
    }

    /**
     * 发布踢出用户事件
     * @param idList 需要踢出的用户id集合
     */
    public void kickOut(List<String> idList) {
        publisher.publishEvent(new KickUserEvent(idList));
    }
}
