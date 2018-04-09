package org.pkaq.web.sys.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

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
}
