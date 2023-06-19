package tech.yunyue.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.yunyue.core.annotation.Ignore;
import tech.yunyue.sys.dict.entity.DictItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典管理子表mapper
 *
 * @author S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface DictItemMapper extends BaseMapper<DictItemEntity> {
    /**
     * 根据mainId查询
     *
     * @return
     */
    List<DictItemEntity> listItemByMainId();
}
