package io.nerv.web.sys.user.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.nerv.core.bizlog.annotation.BizLog;
import io.nerv.core.bizlog.base.BizLogEnum;
import io.nerv.core.enums.LockEnumm;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.core.util.tree.TreeHelper;
import io.nerv.core.exception.OathException;
import io.nerv.web.sys.organization.mapper.OrganizationMapper;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.mapper.UserMapper;
import io.nerv.web.sys.user.vo.UserCenterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 用户登录校验
     * @param userEntity
     * @return
     */
    @BizLog(description = "用户登录", operateType = BizLogEnum.QUERY)
    public UserEntity validate(UserEntity userEntity) throws OathException {
        // 得到客户端传递过来的md5之后的密码
        String pwd = userEntity.getPassword();
        UserEntity ue = new UserEntity();
        ue.setAccount(userEntity.getAccount());
        ue = this.mapper.selectOne(new QueryWrapper<>(ue));

        if (null == ue){
            throw new OathException("用户名或密码错误");
        }

        if(LockEnumm.LOCK.getIndex().equals(ue.getLocked())){
            throw new OathException("该用户已被锁定，请联系管理员");
        }

        if( passwordEncoder.matches(pwd, ue.getPassword())){
           return ue;
        } else {
            throw new OathException("用户名或密码错误");
        }
    }
    /**
     * 查询用户列表
     * @param userEntity
     * @return
     */
    public IPage<UserEntity> listUser(UserEntity userEntity, Integer page) {
        return this.listPage(userEntity, page);
    }

    /**
     * 批量删除用户
     * @param ids
     */
    public void deleteUser(ArrayList<String> ids) {
        this.mapper.deleteBatchIds(ids);
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
        wrapper.in("id",CollectionUtil.join(ids,","));

        this.mapper.update(user, wrapper);
    }

    /**
     * 获取一条用户信息
     * @param id 用户id
     * @return 符合条件的用户对象
     */
    public UserCenterVO getUser(String id) {
        UserCenterVO userCenterVO = new UserCenterVO();
        BeanUtil.copyProperties(this.mapper.selectById(id), userCenterVO);
        return userCenterVO;
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
        this.merge(user);
        return this.listUser(null, 1);
    }
    /**
     * 校验账号是否唯一
     * @param user
     * @return
     */
    public boolean checkUnique(UserEntity user) {
        QueryWrapper<UserEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.like("code", user.getCode());
        if (StrUtil.isNotBlank(user.getCode()) && StrUtil.isNotBlank(user.getAccount())){
            entityWrapper.or();
        }
        entityWrapper.like("account", user.getAccount());

        int records = this.mapper.selectCount(entityWrapper);
        return records>0;
    }

    /**
     * 获取当前登录用户的信息(菜单.权限.消息
     * @param uid 用户ID
     * @return
     */
    public UserEntity fetch(String uid) throws OathException {
        UserEntity userEntity = this.mapper.getUserWithModuleAndRoleById(uid);

        if (null == userEntity){
            throw new OathException("查询不到该用户");
        }

        if (CollUtil.isEmpty(userEntity.getRoles()) || CollUtil.isEmpty(userEntity.getModules())){
            throw new OathException("用户权限不足，请联系管理员");
        }

        List<BaseTreeEntity> treeModule = new TreeHelper().bulid(userEntity.getModules());
        userEntity.setModules(treeModule);

        return userEntity;
    }

    /**
     * 新增/编辑用户信息
     * @param user 用户对象
     */
    public void saveUserForOther(UserEntity user) {
        // 用户资料发生修改后 重新生成密码
        // 这里传递过来的密码是进行md5加密后的
        String pwd = user.getPassword();
        if (StrUtil.isNotBlank(pwd)){
            pwd = passwordEncoder.encode(pwd);
            user.setPassword(pwd);
        }
        this.merge(user);
    }
}
