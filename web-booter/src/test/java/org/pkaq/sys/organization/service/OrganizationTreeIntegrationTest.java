package org.pkaq.sys.organization.service;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class OrganizationTreeIntegrationTest {
    private static final long ROOT_A = 9100000000000000001L;
    private static final long ROOT_B = 9100000000000000002L;
    private static final long MOVED = 9100000000000000003L;
    private static final long CHILD = 9100000000000000004L;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OrganizationService service;

    @Test
    void refreshesDescendantPathAndParentNameAfterMoveAndRename() {
        insert(ROOT_A, 0L, "/" + ROOT_A, "Parent A", "PARENT_A", false);
        insert(ROOT_B, 0L, "/" + ROOT_B, "Parent B", "PARENT_B", true);
        insert(MOVED, ROOT_A, "/" + ROOT_A + "/" + MOVED, "Moved", "MOVED", false);
        insert(CHILD, MOVED, "/" + ROOT_A + "/" + MOVED + "/" + CHILD, "Child", "CHILD", true);

        service.edit(bo(MOVED, ROOT_B, "Moved", "MOVED"));

        assertEquals("/" + ROOT_B + "/" + MOVED, path(MOVED));
        assertEquals("/" + ROOT_B + "/" + MOVED + "/" + CHILD, path(CHILD));

        service.edit(bo(ROOT_B, 0L, "Parent B Renamed", "PARENT_B"));
        OrganizationDetailVo moved = service.get(MOVED);
        assertEquals("Parent B Renamed", moved.getParentName());
    }

    private void insert(long id, long pid, String path, String name, String code, boolean leaf) {
        jdbcTemplate.update("""
                INSERT INTO SYS_ORGANIZATION
                    (ID, DELETED, FROZEN, SORT, TENANT_ID, NAME, CODE, PID, PATH, ISLEAF)
                VALUES (?, 0, 0, 1, 0, ?, ?, ?, ?, ?)
                """, id, name, code, pid, path, leaf);
    }

    private OrganizationAoeBo bo(long id, long pid, String name, String code) {
        OrganizationAoeBo bo = new OrganizationAoeBo();
        bo.setId(id);
        bo.setPid(pid);
        bo.setName(name);
        bo.setCode(code);
        return bo;
    }

    private String path(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT PATH FROM SYS_ORGANIZATION WHERE ID = ?", String.class, id);
    }
}
