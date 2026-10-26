package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.ctrl.RoleCtrl;
import org.pkaq.sys.user.bo.RePwdBo;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.pkaq.sys.user.bo.UserPostBo;
import org.pkaq.sys.user.ctrl.UserCtrl;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 改密及授权接口必须记录更新，而非按不存在的id字段误判新增。
 *
 * @author PKAQ
 * @date 2026-10-19
 */
class BizLogOperationTypeTest {
    @Test
    void usesUpdateForPasswordAndExistingAccountRoleRelations() throws ReflectiveOperationException {
        assertEquals(BizLogCodes.UPDATE, annotation(UserCtrl.class, "repwd", RePwdBo.class).operateType());
        assertEquals(BizLogCodes.UPDATE, annotation(UserCtrl.class, "grant", UserGrantBo.class).operateType());
        assertEquals(BizLogCodes.UPDATE, annotation(UserCtrl.class, "grantPost", UserPostBo.class).operateType());
        assertEquals(BizLogCodes.UPDATE,
                annotation(RoleCtrl.class, "grantResource", RoleResourceRefBo.class).operateType());
        assertEquals(BizLogCodes.UPDATE, annotation(RoleCtrl.class, "grantUser", RoleUserRefBo.class).operateType());
    }

    private BizLog annotation(Class<?> controller, String method, Class<?> argument)
            throws ReflectiveOperationException {
        return controller.getMethod(method, argument).getAnnotation(BizLog.class);
    }
}
