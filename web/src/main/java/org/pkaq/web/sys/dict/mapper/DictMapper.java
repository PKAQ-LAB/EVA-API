package org.pkaq.web.sys.dict.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.pkaq.web.sys.organization.entity.OrganizationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典管理mapper
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface DictMapper extends BaseMapper<DictEntity>{
    /**
     * 查询所有符合条件的树
     * @return 符合查询条件的List
     */
    List<DictEntity> listDict();

    /**
     * 根据parentID查询子节点数据
     * @param id parentID
     * @return 符合条件的List
     */
    List<DictEntity> listChildren(String id);

    /**
     * 批量删除（逻辑）字典项
     * @param ids 要删除的ID
     */
    void deleteDict(List<?> ids);
}