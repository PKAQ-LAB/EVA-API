package org.pkaq.sys.dict.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 显式字典投影适配私有表；不忽略原数据权限，不访问不存在的租户列。
 * @author PKAQ
 * @date 2026-11-01
 */
@Mapper
@Repository
public interface DictItemMapper extends BaseMapper<DictItemEntity> {
    /** 调用方必须先核验来源；查询条件仍经原数据权限拦截器。 */
    @Select("SELECT ID, REVISION, DELETED, FROZEN, SORT, CREATE_ID, CREATE_BY, UTC_CREATE, "
            + "MODIFY_ID, MODIFY_BY, UTC_MODIFY, REMARK, MAIN_ID, D_CODE, D_VALUE "
            + "FROM ${schema}.SYS_DICT_ITEM ${ew.customSqlSegment}")
    List<DictItemEntity> selectVisible(@Param("schema") String schema, @Param("ew") Wrapper<DictItemEntity> wrapper);

    /** 服务端审计值与版本显式写入，避免基类租户列隐式展开。 */
    @Insert("INSERT INTO ${schema}.SYS_DICT_ITEM (ID, REVISION, DELETED, FROZEN, SORT, CREATE_ID, CREATE_BY, "
            + "UTC_CREATE, MODIFY_ID, MODIFY_BY, UTC_MODIFY, REMARK, MAIN_ID, D_CODE, D_VALUE) "
            + "VALUES (#{row.id}, 0, 0, #{row.frozen}, #{row.sort}, #{row.createId}, #{row.createBy}, "
            + "#{row.utcCreate}, #{row.modifyId}, #{row.modifyBy}, #{row.utcModify}, #{row.remark}, "
            + "#{row.mainId}, #{row.dCode}, #{row.dValue})")
    int insertPrivate(@Param("schema") String schema, @Param("row") DictItemEntity row);

    /** 先按原scope锁定主项，再比较读取版本；零更新必须触发整批回滚。 */
    @Update("UPDATE ${schema}.SYS_DICT_ITEM SET MAIN_ID = #{row.mainId}, D_CODE = #{row.dCode}, "
            + "D_VALUE = #{row.dValue}, "
            + "FROZEN = #{row.frozen}, SORT = #{row.sort}, REMARK = #{row.remark}, "
            + "MODIFY_ID = #{row.modifyId}, MODIFY_BY = #{row.modifyBy}, UTC_MODIFY = #{row.utcModify}, "
            + "REVISION = COALESCE(REVISION, 0) + 1 "
            + "WHERE ID = #{row.id} AND DELETED = 0 AND COALESCE(REVISION, 0) = #{revision}"
            + " AND FROZEN <> 9999 AND MAIN_ID = #{row.mainId}")
    int updatePrivate(@Param("schema") String schema, @Param("row") DictItemEntity row,
                      @Param("revision") int revision);

    /** 保留逻辑删除和版本条件，受影响行数由服务校验。 */
    @Update("UPDATE ${schema}.SYS_DICT_ITEM SET "
            + "DELETED = CAST(FLOOR(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)) AS bigint), "
            + "REVISION = COALESCE(REVISION, 0) + 1, MODIFY_ID = #{row.modifyId}, "
            + "MODIFY_BY = #{row.modifyBy}, UTC_MODIFY = #{row.utcModify} "
            + "WHERE ID = #{row.id} AND DELETED = 0 AND COALESCE(REVISION, 0) = #{revision}"
            + " AND FROZEN <> 9999 AND MAIN_ID = #{row.mainId}")
    int deletePrivate(@Param("schema") String schema, @Param("row") DictItemEntity row,
                      @Param("revision") int revision);
}
