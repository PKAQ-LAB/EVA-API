package org.pkaq.sys.user.service;

import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.sys.user.bo.RePwdBo;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserCheckBo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;

import java.util.List;
import java.util.Set;

/**
 * @author PKAQ
 */
public interface IUserService {
    void repwd(RePwdBo rePwdBo);

    void delete(Set<Long> param);

    List<UserListVo> listUser(UserQueryBo queryBo);

    PageVo<UserListVo> listPage(UserQueryBo queryBo);

    void updateUser(Set<Long> ids);

    UserDetailVo getUser(Long id);

    void saveUser(UserAoeBo user);

    boolean checkUnique(UserCheckBo user);
}
