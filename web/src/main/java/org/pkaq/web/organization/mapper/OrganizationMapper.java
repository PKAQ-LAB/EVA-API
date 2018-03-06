package org.pkaq.web.organization.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.organization.entity.OrganizationEntity;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrganizationMapper extends BaseMapper<OrganizationEntity>{
}