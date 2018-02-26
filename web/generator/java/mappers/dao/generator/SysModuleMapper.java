package mappers.dao.generator;

import java.util.List;
import org.pkaq.model.generator.SysModuleEntity;

public interface SysModuleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysModuleEntity record);

    SysModuleEntity selectByPrimaryKey(String id);

    List<SysModuleEntity> selectAll();

    int updateByPrimaryKey(SysModuleEntity record);
}