package org.pkaq.core.mybatis.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper
@Repository
public interface MybatisSupporterMapper extends BaseMapper<MybatisBizLogEntity> {

    MybatisBizLogEntity selectLogById(@Param("id") Long id, @Param("tenantId") Long tenantId);

    long countLogs(@Param("tenantId") Long tenantId,
                   @Param("begin") String begin,
                   @Param("end") String end,
                   @Param("operator") String operator,
                   @Param("operateType") String operateType,
                   @Param("mCode") String mCode,
                   @Param("bId") String bId,
                   @Param("includeArchived") boolean includeArchived);

    List<MybatisBizLogEntity> selectLogPage(@Param("tenantId") Long tenantId,
                                            @Param("begin") String begin,
                                            @Param("end") String end,
                                            @Param("operator") String operator,
                                            @Param("operateType") String operateType,
                                            @Param("mCode") String mCode,
                                            @Param("bId") String bId,
                                            @Param("includeArchived") boolean includeArchived,
                                            @Param("offset") long offset,
                                            @Param("limit") long limit);

    boolean tryArchiveLock();

    int archiveBefore(@Param("cutoff") String cutoff, @Param("batchSize") int batchSize);
}
