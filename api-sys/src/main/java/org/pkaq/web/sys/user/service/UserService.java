package org.pkaq.web.sys.user.service;

import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:00
 */
@Service
public class UserService extends BaseService<UserMapper, UserEntity> {
}
