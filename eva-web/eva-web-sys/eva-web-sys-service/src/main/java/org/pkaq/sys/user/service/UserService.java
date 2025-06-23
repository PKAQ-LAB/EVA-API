package org.pkaq.sys.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogEnum;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.mvc.service.ConvertService;
import org.pkaq.core.mybatis.util.Page;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.upload.provider.FileUploadProvider;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.service.UserRoleRefSerivce;
import org.pkaq.sys.user.bo.*;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserService extends ConvertService<UserMapper, UserConvert> implements IUserService {

    private final UserRoleRefSerivce userRoleRefSerivce;

    private final FileUploadProvider fileUploadProvider;

    private final RoleUserMapper roleUserMapper;

    /**
     * 修改密码
     */
    @BizLog(operateType = BizLogEnum.EDIT, description = "更新了密码[{0}]", args = {"param:0.id"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void repwd(RePwdBo rePwdBo) {
        var uid = ThreadUserHelper.getUserId();
        UserEntity userEntity = this.mapper.selectById(uid);

        if (!BCrypt.checkpw(rePwdBo.getOriginPassword(), userEntity.getPassword())) {
            SysCodes.BAD_ORG_PASSWORD.newException();
        }
        userEntity.setPassword(BCrypt.hashpw(rePwdBo.getNewPassword()));
        this.mapper.updateById(userEntity);
    }

    @BizLog(operateType = BizLogEnum.DELETE, description = "删除了用户", args = {"param:0"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void delete(List<String> param) {
        this.mapper.deleteByIds(param);
    }
    /**
     * 查询用户列表 无分页
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "查询了用户列表")
    @Override
    public List<UserListVo> listUser(UserQueryBo queryBo) {
        UserEntity user = this.converter.queryBoToEntity(queryBo);
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(user);
        wrapper.orderByDesc(UserEntity::getModifyBy);
        return this.converter.entityListToVoList(this.mapper.selectList(wrapper));
    }
    /**
     * 列表查询 - 分页
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "查询了用户列表")
    @Override
    public PageVo listPage(UserQueryBo queryBo) {
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(this.converter.queryBoToEntity(queryBo));
        wrapper.orderByDesc(UserEntity::getUtcModify);

        Page<UserEntity> pagination = new Page<>(queryBo.getPageNo(), queryBo.getPageSize());
        return this.mapper.selectPage(pagination, wrapper);
    }
    /**
     * 解锁/锁定用户
     */
    @Override
    public void updateUser(List<String> ids) {
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
    public UserDetailVo getUser(String id) {
        var user = this.mapper.selectById(id);
        if (null == user) {
            SysCodes.CANNOT_FIND_USER.newException();
        }
        var uvo = this.converter.entityToDetilVo(user);
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
    @BizLog(operateType = BizLogEnum.EDIT, description = "更新用户记录[{0}]", args = {"param:0.id"})
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

        // 保存用户
        UserEntity entity = this.converter.boToEntity(user);
        if (isInsert) {
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
        this.mapper.insertOrUpdate(entity);

        // 保存权限
        UserGrantBo userGrantBo = new UserGrantBo();
        userGrantBo.setUserId(userId);
        userGrantBo.setRoleIds(user.getRoleIds());
        this.userRoleRefSerivce.saveRoles(userGrantBo);
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

        if (CharSequenceUtil.isNotBlank(user.getId())) {
            entityWrapper.ne(UserEntity::getId, user.getId());
        }
        return this.mapper.selectCount(entityWrapper) > 0;
    }
}
