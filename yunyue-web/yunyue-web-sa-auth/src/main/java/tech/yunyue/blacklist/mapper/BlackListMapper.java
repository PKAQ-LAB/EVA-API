package tech.yunyue.blacklist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import tech.yunyue.blacklist.entity.BlackListEntity;
import tech.yunyue.core.annotation.Ignore;

/**
 * 黑名单管理mapper
 */
@Mapper
@Repository
@Ignore
public interface BlackListMapper extends BaseMapper<BlackListEntity> {

}
