package org.pkaq.sys.dict.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.springframework.stereotype.Repository;

/**
 *  字典管理子表mapper
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface DictItemMapper extends BaseMapper<DictItemEntity> {
}