package org.pkaq.sys.module.service;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleFrozenBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ModuleTreeIntegrationTest {
    private static final long ROOT = 9200000000000000001L;
    private static final long CHILD = 9200000000000000002L;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ModuleService service;

    @Test
    void rejectsMovingModuleBelowItsDescendantAndPreservesPath() {
        insert(ROOT, 0L, "/" + ROOT, "Root", "ROOT", false);
        insert(CHILD, ROOT, "/" + ROOT + "/" + CHILD, "Child", "CHILD", true);

        assertThrows(RuntimeException.class, () -> service.editModule(bo(ROOT, CHILD, "Root", "ROOT")));
        assertEquals("/" + ROOT, path(ROOT));
    }

    @Test
    void checksCodeUniquenessWithinParentAndExcludesCurrentRecord() {
        insert(ROOT, 0L, "/" + ROOT, "Root", "ROOT", false);
        insert(CHILD, ROOT, "/" + ROOT + "/" + CHILD, "Child", "CHILD", true);

        assertTrue(service.checkUnique(bo(0L, ROOT, "Another", "CHILD")));
        assertFalse(service.checkUnique(bo(CHILD, ROOT, "Child", "CHILD")));
    }

    @Test
    void appliesExplicitFrozenTargetIdempotently() {
        insert(ROOT, 0L, "/" + ROOT, "Root", "ROOT", false);
        ModuleFrozenBo bo = new ModuleFrozenBo();
        bo.setParam(java.util.Set.of(ROOT));
        bo.setFrozen(1);

        service.switchFrozen(bo);
        service.switchFrozen(bo);

        assertEquals(1, frozen(ROOT));
    }

    @Test
    void preservesDirectReadOnlyModule() {
        insert(ROOT, 0L, "/" + ROOT, "Root", "ROOT", true);
        jdbcTemplate.update("UPDATE SYS_MODULE SET FROZEN = 9999 WHERE ID = ?", ROOT);
        ModuleFrozenBo bo = frozenBo(ROOT, 1);

        service.switchFrozen(bo);

        assertEquals(9999, frozen(ROOT));
    }

    @Test
    void preservesReadOnlyDescendantDuringCascade() {
        insert(ROOT, 0L, "/" + ROOT, "Root", "ROOT", false);
        insert(CHILD, ROOT, "/" + ROOT + "/" + CHILD, "Child", "CHILD", true);
        jdbcTemplate.update("UPDATE SYS_MODULE SET FROZEN = 9999 WHERE ID = ?", CHILD);

        service.switchFrozen(frozenBo(ROOT, 1));

        assertEquals(1, frozen(ROOT));
        assertEquals(9999, frozen(CHILD));
    }

    @Test
    void rejectsEditingDeletingAndSortingReadOnlyModule() {
        insert(ROOT, 0L, "/" + ROOT, "Root", "ROOT", true);
        jdbcTemplate.update("UPDATE SYS_MODULE SET FROZEN = 9999 WHERE ID = ?", ROOT);
        ModuleSortBo sortBo = new ModuleSortBo();
        sortBo.setId(ROOT);
        sortBo.setOldSort(1);
        sortBo.setNewSort(2);

        assertThrows(RuntimeException.class, () -> service.editModule(bo(ROOT, 0L, "Changed", "ROOT")));
        assertThrows(RuntimeException.class, () -> service.sortModule(sortBo));
        assertThrows(RuntimeException.class, () -> service.deleteModule(java.util.Set.of(ROOT)));
        assertEquals("Root", jdbcTemplate.queryForObject(
                "SELECT NAME FROM SYS_MODULE WHERE ID = ?", String.class, ROOT));
    }

    private void insert(long id, long pid, String path, String name, String code, boolean leaf) {
        jdbcTemplate.update("""
                INSERT INTO SYS_MODULE
                    (ID, DELETED, FROZEN, SORT, TENANT_ID, NAME, CODE, PID, PATH, ISLEAF,
                     ROUTE_URL, COMPONENT_URL)
                VALUES (?, 0, 0, 1, 0, ?, ?, ?, ?, ?, ?, ?)
                """, id, name, code, pid, path, leaf, "/route/" + code, "component/" + code);
    }

    private ModuleAoeBo bo(long id, long pid, String name, String code) {
        ModuleAoeBo bo = new ModuleAoeBo();
        if (id != 0L) {
            bo.setId(id);
        }
        bo.setPid(pid);
        bo.setName(name);
        bo.setCode(code);
        bo.setRouteUrl("/route/" + code);
        bo.setComponentUrl("component/" + code);
        return bo;
    }

    private String path(long id) {
        return jdbcTemplate.queryForObject("SELECT PATH FROM SYS_MODULE WHERE ID = ?", String.class, id);
    }

    private Integer frozen(long id) {
        return jdbcTemplate.queryForObject("SELECT FROZEN FROM SYS_MODULE WHERE ID = ?", Integer.class, id);
    }

    private ModuleFrozenBo frozenBo(long id, int frozen) {
        ModuleFrozenBo bo = new ModuleFrozenBo();
        bo.setParam(java.util.Set.of(id));
        bo.setFrozen(frozen);
        return bo;
    }
}
