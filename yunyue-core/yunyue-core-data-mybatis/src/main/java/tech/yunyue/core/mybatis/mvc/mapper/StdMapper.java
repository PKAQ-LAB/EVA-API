package tech.yunyue.core.mybatis.mvc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import tech.yunyue.core.mybatis.mvc.entity.mybatis.StdEntity;
/**
 * <h1>空接口 为了让Mybatis-plus缓存StdEntity的字段信息
 * <p>
 * 在使用存在父类的泛型的Lambda表达式时会报错：
 * {@code MybatisPlusException: can not find lambda cache for this entity [tech.yunyue.core.mybatis.mvc.entity.mybatis.StdEntity]}
 * <p>
 * 原因是MybatisPlus3.2+之后不会缓存实体类的父类字段信息
 * 因此我们单独为{@link StdEntity}添加一个的Mapper类，这样他就会缓存该类的信息了。
 */
@Mapper
@Repository
public interface StdMapper extends BaseMapper<StdEntity> {
}
