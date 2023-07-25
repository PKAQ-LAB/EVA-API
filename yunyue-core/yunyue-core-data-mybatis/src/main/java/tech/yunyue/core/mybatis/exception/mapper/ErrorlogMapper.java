package tech.yunyue.core.mybatis.exception.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.yunyue.core.mybatis.exception.entity.MybatisErrorlogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface ErrorlogMapper extends BaseMapper<MybatisErrorlogEntity> {
}
