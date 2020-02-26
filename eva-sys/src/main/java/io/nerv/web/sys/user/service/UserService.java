package io.nerv.web.sys.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.OathException;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.core.upload.util.FileUploadProvider;
import io.nerv.core.util.SecurityHelper;
import io.nerv.core.util.tree.TreeHelper;
import io.nerv.web.sys.dict.cache.DictCacheHelper;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.mapper.ModuleMapper;
import io.nerv.web.sys.organization.mapper.OrganizationMapper;
import io.nerv.web.sys.role.entity.RoleUserEntity;
import io.nerv.web.sys.role.mapper.RoleUserMapper;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.mapper.UserMapper;
import io.nerv.web.sys.user.vo.PasswordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户管理
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:00
 */
@Service
public class UserService extends StdBaseService<UserMapper, UserEntity> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private RoleUserMapper roleUserMapper;

    @Autowired
    private FileUploadProvider fileUploadProvider;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private DictCacheHelper dictCacheHelper;

    @Autowired
    private SecurityHelper securityHelper;

    /**
     * 修改密码
     * @param passwordVO
     * @return
     */
    public boolean repwd(PasswordVO passwordVO){
        String userid = this.securityHelper.getJwtUserId();
        UserEntity userEntity = this.mapper.selectById(userid);
        if (passwordEncoder.matches(passwordVO.getOriginpassword(), userEntity.getPassword())){
            userEntity.setPassword(passwordEncoder.encode(passwordVO.getNewpassword()));
            this.mapper.updateById(userEntity);
            return true;
        }
        return false;
    }


    /**
     * 查询用户列表
     * @param userEntity
     * @return
     */
    public IPage<UserEntity> listUser(UserEntity userEntity, Integer page) {
        page = null != page ? page : 1;

        Page pagination = new Page();
        pagination.setCurrent(page);

        return this.mapper.getUerWithRoleId(pagination, userEntity);
    }

    /**
     * 查询用户列表 无分页
     * @param userEntity
     * @return
     */
    public List<UserEntity> listUser(UserEntity userEntity) {
        return this.list(userEntity);
    }

    /**
     * 解锁/锁定用户
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
     * @param user 用户对象
     * @return 用户列表
     */
    public IPage<UserEntity> saveUser(UserEntity user) {
        // 用户资料发生修改后 重新生成密码
        // 这里传递过来的密码是进行md5加密后的
        String pwd = user.getPassword();
        if (StrUtil.isNotBlank(pwd)){
            pwd = passwordEncoder.encode(pwd);
            user.setPassword(pwd);
        }
        //设置部门名称
        if(StrUtil.isNotBlank(user.getDeptId())){
            user.setDeptName(organizationMapper.selectById(user.getDeptId()).getName());
        }

        // 新增手工生成主键
        // 编辑， 删除原有头像文件，保存新的头像文件
        String userId = user.getId();
        boolean isInsert = true;
        if (StrUtil.isBlank(userId)){
            userId = IdWorker.getIdStr();
            user.setId(userId);
        } else {
            isInsert = false;
            UserEntity oldUser = this.mapper.selectById(userId);
            String avatar = oldUser.getAvatar();
            if (StrUtil.isNotBlank(avatar) && !avatar.equals(user.getAvatar())){
                fileUploadProvider.delFromStorage(avatar);
            }
        }

        // 保存新的头像文件
        if (StrUtil.isNotBlank(user.getAvatar())){
            fileUploadProvider.storageWithThumbnail(0.3f, user.getAvatar());
        }
        // 保存权限
        this.saveRoles(user);

        if (isInsert){
            this.mapper.insert(user);
        } else {
            this.mapper.updateById(user);
        }
        this.merge(user);
        return this.listPage(null, 1);
    }

    /**
     * 校验账号是否唯一
     * @param user
     * @return
     */
    public boolean checkUnique(UserEntity user) {
        QueryWrapper<UserEntity> entityWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(user.getAccount())){
            entityWrapper.eq("account", user.getAccount());
        }
        if (StrUtil.isNotBlank(user.getId())){
            entityWrapper.ne("id", user.getId());
        }
        int records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 获取当前登录用户的信息(菜单.权限.消息
     * @param uid 用户ID
     * @return
     */
    public Map<String, Object> fetch(String uid) throws OathException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(uid);

        userEntity = this.mapper.getUserWithRole(userEntity);

        if (null == userEntity){
            throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }

        List<ModuleEntity> moduleEntity = this.moduleMapper.getRoleModuleByUserId(userEntity.getId());
        List<BaseTreeEntity> treeModule = new TreeHelper().bulid(moduleEntity);

        if (CollUtil.isEmpty(userEntity.getRoles()) || CollUtil.isEmpty(treeModule)){
            throw new OathException(BizCodeEnum.PERMISSION_EXPIRED);
        }

        return Map.of("user", userEntity, "menus", treeModule, "dict", dictCacheHelper.getAll());
    }

    /**
     * 保存用户权限
     * @param user
     */
    public void saveRoles(UserEntity user){
        // 保存权限
        if (CollUtil.isNotEmpty(user.getRoles())){
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
