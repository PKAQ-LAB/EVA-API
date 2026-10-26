package org.pkaq.sys.module.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.entity.ModuleEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模块列表的实际 MyBatis 映射回归。
 *
 * @author Codex
 * @date 2026-10-18
 */
class ModuleListMappingTest {
    @Test
    void loadsFlatRowsForServiceTreeAssembly() throws IOException {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.getTypeAliasRegistry().registerAlias("module", ModuleEntity.class);
        for (String resource : new String[]{"mapper/common.xml", "mapper/Module.xml"}) {
            try (InputStream stream = Resources.getResourceAsStream(resource)) {
                new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }

        var statement = configuration.getMappedStatement(ModuleMapper.class.getName() + ".selectModuleMapList");
        var resultMap = statement.getResultMaps().getFirst();

        assertEquals(ModuleEntity.class, resultMap.getType());
        assertFalse(resultMap.getResultMappings().stream().anyMatch(mapping -> mapping.getNestedQueryId() != null));
        String query = statement.getBoundSql(Map.of("bo", new ModuleQueryBo())).getSql().toUpperCase();
        assertTrue(query.contains("T.ROUTE_URL"));
        assertTrue(query.contains("T.ICON"));
    }
}
