package org.pkaq.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  字典管理子表mapper
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface DictItemMapper extends BaseMapper<DictItemEntity> {
    /** 根据mainId查询
     * @return
     */
    List<DictItemEntity> listItemByMainId();
}