package io.nerv.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.sys.dict.entity.DictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典管理mapper
 *
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface DictMapper extends BaseMapper<DictEntity> {
    /**
     * 查询所有符合条件的树
     *
     * @return 符合查询条件的List
     */
    List<DictEntity> listDict();

    /**
     * 根据parentID查询子节点数据
     *
     * @param id parentID
     * @return 符合条件的List
     */
    List<DictEntity> listChildren(String id);

    /**
     * 根据ID加载一条字典详情
     *
     * @param id
     * @return
     */
    DictEntity getDict(String id);
}