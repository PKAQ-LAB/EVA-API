package mappers.dao.generator;

import java.util.List;
import org.pkaq.model.generator.SysOrginizationEntity;

public interface SysOrginizationMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysOrginizationEntity record);

    SysOrginizationEntity selectByPrimaryKey(String id);

    List<SysOrginizationEntity> selectAll();

    int updateByPrimaryKey(SysOrginizationEntity record);
}