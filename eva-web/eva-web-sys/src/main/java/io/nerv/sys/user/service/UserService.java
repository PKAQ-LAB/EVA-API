package io.nerv.sys.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.nerv.sys.dict.cache.DictCacheHelper;
import io.nerv.sys.module.entity.ModuleEntityStd;
import io.nerv.sys.module.mapper.ModuleMapper;
import io.nerv.sys.organization.mapper.OrganizationMapper;
import io.nerv.sys.role.entity.RoleUserEntity;
import io.nerv.sys.role.mapper.RoleUserMapper;
import io.nerv.sys.user.entity.UserEntity;
import io.nerv.sys.user.mapper.UserMapper;
import io.nerv.sys.user.vo.PasswordVO;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdTreeEntity;
import io.nerv.core.mybatis.mvc.service.mybatis.StdService;
import io.nerv.core.mybatis.mvc.util.Page;
import io.nerv.core.mybatis.util.tree.TreeHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理
 *
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:00
 */
@Service
public class UserService extends StdService<UserMapper, UserEntity> {

    private final OrganizationMapper organizationMapper;

    private final RoleUserMapper roleUserMapper;

    private final FileUploadProvider fileUploadProvider;

    private final ModuleMapper moduleMapper;

    private final DictCacheHelper dictCacheHelper;

    public UserService(OrganizationMapper organizationMapper, RoleUserMapper roleUserMapper, FileUploadProvider fileUploadProvider, ModuleMapper moduleMapper, DictCacheHelper dictCacheHelper) {
        this.organizationMapper = organizationMapper;
        this.roleUserMapper = roleUserMapper;
        this.fileUploadProvider = fileUploadProvider;
        this.moduleMapper = moduleMapper;
        this.dictCacheHelper = dictCacheHelper;
    }

    /**
     * 修改密码
     *
     * @param passwordVO
     * @return
     */
    public boolean repwd(PasswordVO passwordVO) {
        //TODO 获取用户ID
        UserEntity userEntity = this.mapper.selectById(passwordVO.getUserId());

        if (BCrypt.checkpw(passwordVO.getOriginpassword(), userEntity.getPassword())) {
            userEntity.setPassword(BCrypt.hashpw(passwordVO.getNewpassword()));
            this.mapper.updateById(userEntity);
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
    public List<UserEntity> listUser(UserEntity userEntity) {
        return this.list(userEntity);
    }

    /**
     * 解锁/锁定用户
     *
     * @param ids
     * @param lock
     */
    public void updateUser(ArrayList<String> ids, String lock) {
        UserEntity user = new UserEntity();
        user.setLocked(lock);
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);

        this.mapper.update(user, wrapper);
    }

    /**
     * 获取一条用户信息
     *
     * @param id 用户id
     * @return 符合条件的用户对象
     */
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
    public void saveUser(UserEntity user) {
        // 用户资料发生修改后 重新生成密码
        // 这里传递过来的密码是进行md5加密后的
        String pwd = user.getPassword();
        if (StrUtil.isNotBlank(pwd)) {
            pwd = BCrypt.hashpw(pwd);
            user.setPassword(pwd);
        }
        //设置部门名称
        if (StrUtil.isNotBlank(user.getDeptId())) {
            user.setDeptName(organizationMapper.selectById(user.getDeptId()).getName());
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
    public List<StdTreeEntity> fetch(String uid) {

        List<ModuleEntityStd> moduleEntity = this.moduleMapper.getRoleModuleByUserId(uid);
        List<StdTreeEntity> treeModule = new TreeHelper().bulid(moduleEntity);

        BizCodeEnum.PERMISSION_EXPIRED.assertNotBlank(treeModule);

        return treeModule;
    }

    /**
     * 保存用户权限
     *
     * @param user
     */
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
}
