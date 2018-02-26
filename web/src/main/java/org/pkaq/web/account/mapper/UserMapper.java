package org.pkaq.web.account.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.account.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper
@Repository
public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserEntity record);

    UserEntity selectByPrimaryKey(String id);

    List<UserEntity> selectAll();

    int updateByPrimaryKey(UserEntity record);
}