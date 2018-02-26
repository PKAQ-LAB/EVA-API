package mappers.dao.generator;

import java.util.List;
import org.pkaq.model.generator.SysUserInfoEntity;

public interface SysUserInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysUserInfoEntity record);

    SysUserInfoEntity selectByPrimaryKey(String id);

    List<SysUserInfoEntity> selectAll();

    int updateByPrimaryKey(SysUserInfoEntity record);
}