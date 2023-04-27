package tech.yunyue.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.yunyue.sys.role.entity.RoleUserEntity;
import tech.yunyue.core.annotation.Ignore;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 角色用户关系mapper
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleUserMapper extends BaseMapper<RoleUserEntity> {

}
