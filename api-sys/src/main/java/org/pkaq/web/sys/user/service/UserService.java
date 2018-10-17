package org.pkaq.web.sys.user.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.pkaq.core.bizlog.annotation.BizLog;
import org.pkaq.core.bizlog.base.BizLogEnum;
import org.pkaq.core.enums.LockEnumm;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 用户管理
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:00
 */
@Service
public class UserService extends BaseService<UserMapper, UserEntity> {
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录校验
     * @param userEntity
     * @return
     */
    @BizLog(description = "用户登录", operateType = BizLogEnum.QUERY)
    public UserEntity validate(UserEntity userEntity){
        // 得到客户端传递过来的md5之后的密码
        String pwd = userEntity.getPassword();
        UserEntity ue = new UserEntity();
        ue.setAccount(userEntity.getAccount());
        ue = this.mapper.selectOne(new QueryWrapper<>(ue));
        // 签发token
        // TODO 根据用户名密码查询权限信息 存入redis
        if(null != ue && passwordEncoder.matches(pwd, ue.getPassword())){
           return this.mapper.getUserWithModuleAndRoleById(ue.getId());
        } else {
            return null;
        }
    }

    /**
     * 查询用户列表
     * @param userEntity
     * @return
     */
    @BizLog(description = "用户新增")
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
        user.setLocked(LockEnumm.LOCK.getIndex().equals(lock));
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id",CollectionUtil.join(ids,","));

        this.mapper.update(user, wrapper);
    }

    /**
     * 获取一条用户信息
     * @param id 用户id
     * @return 符合条件的用户对象
     */
    public UserEntity getUser(String id) {
        return this.mapper.selectById(id);
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
}
