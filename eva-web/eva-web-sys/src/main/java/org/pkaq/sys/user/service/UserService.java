package org.pkaq.sys.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.Page;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.sys.SYSCode;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.PasswordVO;
import org.pkaq.sys.user.vo.UserListVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserService extends StdService<UserMapper, UserEntity> {
    private final UserConvert userConvert;


    private final RoleUserMapper roleUserMapper;

    private final FileUploadProvider fileUploadProvider;

    private final ModuleMapper moduleMapper;

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
    public IPage<UserListVo> listUser(UserAoeBo userEntity, Integer page, Integer size) {
        page = null != page ? page : 1;
        size = null != size ? size : 30;

        var pagination = new Page<>();
        pagination.setCurrent(page);
        pagination.setSize(size);

        return   this.mapper.getUerWithRoleId(pagination, userEntity);
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
        user.setFrozen(lock);
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
    public void saveUser(UserAoeBo user) {
        // 用户资料发生修改后 重新生成密码
        // 这里传递过来的密码是进行md5加密后的
        String pwd = user.getPassword();
        if (CharSequenceUtil.isNotBlank(pwd)) {
            pwd = BCrypt.hashpw(pwd);
            user.setPassword(pwd);
        }

        // 新增手工生成主键
        // 编辑， 删除原有头像文件，保存新的头像文件
        String userId = user.getId();
        boolean isInsert = true;
        if (CharSequenceUtil.isBlank(userId)) {
            userId = IdWorker.getIdStr();
            user.setId(userId);
        } else {
            isInsert = false;
            UserEntity oldUser = this.mapper.selectById(userId);
            String avatar = oldUser.getAvatar();
            if (CharSequenceUtil.isNotBlank(avatar) && !avatar.equals(user.getAvatar())) {
                fileUploadProvider.delFromStorage(avatar);
            }
        }

        // 保存新的头像文件
        if (CharSequenceUtil.isNotBlank(user.getAvatar())) {
            fileUploadProvider.storageWithThumbnail(0.3f, user.getAvatar());
        }
        // 保存权限
        UserEntity entity = userConvert.boToEntity(user);
        this.saveRoles(user);

        if (isInsert) {
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
        this.merge(entity);
    }

    /**
     * 校验账号是否唯一
     *
     * @param user
     * @return
     */
    public boolean checkUnique(UserAoeBo user) {
        QueryWrapper<UserEntity> entityWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(user.getAccount())) {
            entityWrapper.eq("account", user.getAccount());
        }
        if (CharSequenceUtil.isNotBlank(user.getId())) {
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

        List<ModuleEntity> moduleEntity = this.moduleMapper.getRoleModuleByUserId(uid);
        List<StdTreeEntity> treeModule = new TreeHelper().bulid(moduleEntity);

        SYSCode.PERMISSION_EXPIRED.assertNotBlank(treeModule);

        return treeModule;
    }

    /**
     * 保存用户权限
     *
     * @param user
     */
    public void saveRoles(UserAoeBo user) {
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
