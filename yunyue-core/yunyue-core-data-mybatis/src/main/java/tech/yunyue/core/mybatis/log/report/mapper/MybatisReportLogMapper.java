package tech.yunyue.core.mybatis.log.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import tech.yunyue.core.mybatis.log.report.entity.MybatisReportLogEntity;

@Mapper
@Repository
public interface MybatisReportLogMapper extends BaseMapper<MybatisReportLogEntity> {
}
