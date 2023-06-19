package tech.yunyue.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.yunyue.core.annotation.Ignore;
import tech.yunyue.sys.module.entity.ModuleResources;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 模块-资源module
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface ModuleResourceMapper extends BaseMapper<ModuleResources> {

}
