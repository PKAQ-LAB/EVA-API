package org.pkaq.web.account.service;

import org.pkaq.web.account.entity.UserEntity;
import org.pkaq.web.account.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA.
 * Datetime: 2018/2/9 8:10
 * @author PKAQ
 */
@Service
@Transactional
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public UserEntity loginService(UserEntity userEntity){
       return this.userMapper.selectByPrimaryKey("0");
    }
}
