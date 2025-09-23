package org.pkaq.sys.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.entity.Entity;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.core.mybatis.mvc.service.ConvertService;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.service.UserPostRefSerivce;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.service.UserRoleRefSerivce;
import org.pkaq.sys.user.bo.*;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.pkaq.sys.user.vo.UserResourceVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 用户管理
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserService extends ConvertService<UserMapper, UserConvert> implements IUserService {

    private final UserRoleRefSerivce userRoleRefSerivce;

    private final UserPostRefSerivce userPostRefSerivce;

    private final FileProvider fileProvider;

    private final RoleUserMapper roleUserMapper;

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void repwd(RePwdBo rePwdBo) {
        var uid = ThreadUserHelper.getUserId();

        UserEntity userEntity = this.mapper.selectById(uid);

        if (!BCrypt.checkpw(rePwdBo.getOriginPassword(), userEntity.getPassword())) {
            SysCodes.BAD_ORG_PASSWORD.newException();
        }

        UserEntity updateE = new UserEntity();
        updateE.setId(userEntity.getId());
        updateE.setPassword(BCrypt.hashpw(rePwdBo.getNewPassword()));
        updateE.setRevision(rePwdBo.getRevision());

        this.mapper.updateById(updateE);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void delete(Set<Long> param) {
        this.mapper.deleteByIds(param);
        // 删除授权关系
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().in(RoleUserEntity::getUserId, param));
    }

    /**
     * 查询用户列表 无分页
     */
    @Override
    public List<UserListVo> listUser(UserQueryBo queryBo) {
        UserEntity user = this.converter.boToEntity(queryBo);
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(user);
        wrapper.orderByDesc(UserEntity::getModifyBy);
        return this.converter.entityToListVo(this.mapper.selectList(wrapper));
    }

    public List<? extends Vo> listUser(UserQueryBo queryBo, Function<List<? extends Entity>, List<? extends Vo>> convert) {
        UserEntity user = this.converter.boToEntity(queryBo);
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(user);
        wrapper.orderByDesc(UserEntity::getModifyBy);
        return convert.apply(this.mapper.selectList(wrapper));
    }

    /**
     * 列表查询 - 分页
     */
    @Override
    public PageVo<UserListVo> listPage(UserQueryBo queryBo) {
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(this.converter.boToEntity(queryBo));
        wrapper.orderByDesc(UserEntity::getUtcModify);
        PageResult<UserEntity> pagination = new PageResult<>(queryBo.getPageNo(), queryBo.getPageSize());
        return this.mapper.selectPage(pagination, wrapper).map(this.converter::entityToListVo);
    }

    /**
     * 解锁/锁定用户
     */
    @Override
    public void updateUser(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            CommonCodes.NULL_ID.newException();
        }

        this.mapper.change(ids);
    }

    /**
     * 获取一条用户信息
     *
     * @param id 用户id
     * @return 符合条件的用户对象
     */
    @Override
    public UserDetailVo getUser(Long id) {
        var user = this.mapper.selectById(id);
        if (null == user) {
            SysCodes.CANNOT_FIND_USER.newException();
        }
        var uvo = this.converter.entityToDetailVo(user);
        // 权限列表
        var roleIds = this.roleUserMapper.selectRoleIds(id);

        uvo.setRoleIds(roleIds);

        return uvo;
    }

    /**
     * 新增/编辑用户信息
     *
     * @param user 用户对象
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void saveUser(UserAoeBo user) {
        // 用户资料发生修改后 重新生成密码
        // 这里传递过来的密码是进行md5加密后的
        String pwd = user.getPassword();
        pwd = BCrypt.hashpw(pwd);
        user.setPassword(pwd);

        // 新增手工生成主键
        // 编辑， 删除原有头像文件，保存新的头像文件
        long userId = user.getId();
        boolean isInsert = true;
        if (0 == userId) {
            userId = IdWorker.getId();
            user.setId(userId);
            var tid = ThreadUserHelper.getTenantId();
            var leftCt = this.mapper.availableCounts(tid);
            if (leftCt < 1) {
                SysCodes.USER_ACCOUNT_LIMIT.newException();
            }
        } else {
            isInsert = false;
            UserEntity oldUser = this.mapper.selectById(userId);
            String avatar = oldUser.getAvatar();
            if (CharSequenceUtil.isNotBlank(avatar) && !avatar.equals(user.getAvatar())) {
                fileProvider.delFromStorage(avatar);
            }
        }

        // 保存新的头像文件
        if (CharSequenceUtil.isNotBlank(user.getAvatar())) {
            fileProvider.storageWithThumbnail(0.3f, user.getAvatar());
        }

        // 保存用户
        UserEntity entity = this.converter.boToEntity(user);
        if (isInsert) {
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }

        // 保存权限
        if (CollUtil.isNotEmpty(user.getRoleIds())) {
            UserGrantBo userGrantBo = new UserGrantBo();
            userGrantBo.setUserId(userId);
            userGrantBo.setRoleIds(user.getRoleIds());
            this.userRoleRefSerivce.saveRoles(userGrantBo);
        }

        // 保存岗位
        if (CollUtil.isNotEmpty(user.getRoleIds())) {
            UserPostBo postBo = new UserPostBo();
            postBo.setUserId(userId);
            postBo.setPostIds(user.getPostId());
            this.userPostRefSerivce.savePosts(postBo);
        }
    }

    /**
     * 获取当前登录用户的信息(菜单.权限.消息
     * todo
     * @param uid 用户ID
     */
    public List<UserResourceVo> fetchModuleByUid(Long uid) {
//        // 菜单树
//        List<ModuleEntity> moduleEntity = this.mapper.getRoleModuleByUserId(uid);
//        List<StdTreeEntity> treeModule = TreeHelper().bulid(moduleEntity);
//
//        List<UserResourceVo> urv = this.convert.moduleTreeToUserResourceVo(treeModule);
//        // 权限是否为空
////        SysCodes.PERMISSION_EXPIRED.assertNotBlank(treeModule);
//
//        return urv;
        return null;
    }

    /**
     * 校验账号是否唯一
     */
    @Override
    public boolean checkUnique(UserCheckBo user) {
        LambdaQueryWrapper<UserEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.nested(w ->
                w.eq(UserEntity::getAccount, user.getAccount())
                        .or()
                        .eq(UserEntity::getCode, user.getCode()));

        if (null != user.getId() && user.getId() != 0) {
            entityWrapper.ne(UserEntity::getId, user.getId());
        }
        return this.mapper.selectCount(entityWrapper) > 0;
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void createTenantAdmin(UserEntity user) {
        String pwd = user.getPassword();
        pwd = BCrypt.hashpw(pwd);
        user.setPassword(pwd);
        this.mapper.insert(user);
    }
}
