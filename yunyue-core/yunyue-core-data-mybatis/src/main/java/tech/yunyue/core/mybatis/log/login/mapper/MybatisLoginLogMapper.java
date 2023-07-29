package tech.yunyue.core.mybatis.log.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import tech.yunyue.core.mybatis.log.login.entity.MybatisLoginLogEntity;

@Mapper
@Repository
public interface MybatisLoginLogMapper extends BaseMapper<MybatisLoginLogEntity> {
}
