package org.pkaq.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.module.entity.ModuleResources;
import org.springframework.stereotype.Repository;

/**
 * 模块-资源module
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface ModuleResourceMapper extends BaseMapper<ModuleResources> {

}
