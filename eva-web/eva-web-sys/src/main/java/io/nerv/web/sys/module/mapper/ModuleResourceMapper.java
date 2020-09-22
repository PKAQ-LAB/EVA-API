package io.nerv.web.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.web.sys.module.entity.ModuleResources;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 模块-资源module
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:17
 */
@Mapper
@Repository
public interface ModuleResourceMapper extends BaseMapper<ModuleResources> {

}
