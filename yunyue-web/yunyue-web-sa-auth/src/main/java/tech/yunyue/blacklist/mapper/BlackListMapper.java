package tech.yunyue.blacklist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import tech.yunyue.blacklist.entity.BlackListEntity;

/**
 * 黑名单管理mapper
 */
@Mapper
@Repository
public interface BlackListMapper extends BaseMapper<BlackListEntity> {

}
