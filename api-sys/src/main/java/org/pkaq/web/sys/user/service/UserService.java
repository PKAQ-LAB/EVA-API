package org.pkaq.web.sys.user.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.core.util.Response;
import org.pkaq.web.sys.organization.entity.OrganizationEntity;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.entity.UserEnumm;
import org.pkaq.web.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:00
 */
@Service
public class UserService extends BaseService<UserMapper, UserEntity> {
    /**
     * 查询用户列表
     * @param userEntity
     * @return
     */
    public List<UserEntity> listUser(UserEntity userEntity) {
        Wrapper<UserEntity> wrapper = new EntityWrapper<>(userEntity);
        return this.mapper.selectList(wrapper);
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
        user.setLocked(UserEnumm.LOCK.getIndex().equals(lock));
        Wrapper<UserEntity> wrapper = new EntityWrapper<>();
        wrapper.in("id",CollectionUtil.join(ids,","));

        this.mapper.update(user, wrapper);
    }
}
