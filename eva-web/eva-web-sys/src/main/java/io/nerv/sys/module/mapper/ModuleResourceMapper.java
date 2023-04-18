package io.nerv.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.sys.module.entity.ModuleResources;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 模块-资源module
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface ModuleResourceMapper extends BaseMapper<ModuleResources> {

}
