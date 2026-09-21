package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.sys.dict.ctrl.DictCtrl;
import org.pkaq.sys.module.service.ModuleService;
import org.pkaq.sys.role.ctrl.RoleCtrl;
import org.pkaq.sys.tenant.ctrl.TenantCtrl;
import org.pkaq.sys.tenant.pkg.service.TenantPackageService;
import org.pkaq.sys.tenant.service.TenantService;
import org.pkaq.sys.user.ctrl.UserCtrl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统管理接口契约注解测试。
 *
 * @author PKAQ
 */
class ManagementContractAnnotationTest {

    @Test
    void roleManagementExposesClosedCrudEndpoints() throws Exception {
        assertPostMapping(RoleCtrl.class.getMethod("checkUnique", org.pkaq.core.mvc.bo.IdCodeBo.class),
                "/checkUnique");
        assertPostMapping(RoleCtrl.class.getMethod("change", org.pkaq.core.mvc.bo.SingleArray.class),
                "/switch");
        assertNotNull(RoleCtrl.class.getMethod("get", long.class));
        assertBizLog(RoleCtrl.class.getMethod("list", org.pkaq.sys.role.bo.RoleQueryBo.class),
                BizLogCodes.QUERY);
        assertBizLog(RoleCtrl.class.getMethod("del", org.pkaq.core.mvc.bo.SingleArray.class),
                BizLogCodes.DELETE);
    }

    @Test
    void userQueriesAndDeleteUseAccurateAuditTypes() throws Exception {
        assertBizLog(UserCtrl.class.getMethod("list", org.pkaq.sys.user.bo.UserQueryBo.class),
                BizLogCodes.QUERY, "param:0");
        assertBizLog(UserCtrl.class.getMethod("get", Long.class), BizLogCodes.QUERY, "param:0");
        assertBizLog(UserCtrl.class.getMethod("del", org.pkaq.core.mvc.bo.SingleArray.class),
                BizLogCodes.DELETE);
        assertBizLog(UserCtrl.class.getMethod("change", org.pkaq.core.mvc.bo.SingleArray.class),
                BizLogCodes.UPDATE);
        assertBizLog(TenantCtrl.class.getMethod("change", org.pkaq.core.mvc.bo.SingleArray.class),
                BizLogCodes.UPDATE);
    }

    @Test
    void dictionaryWritesAreAuditedAndPostDeleteIsAvailable() throws Exception {
        Method delete = DictCtrl.class.getMethod("deleteDict", Long.class);
        assertPostMapping(delete, "/del/{id}");
        assertBizLog(delete, BizLogCodes.DELETE);
        assertBizLog(DictCtrl.class.getMethod("editDict", org.pkaq.sys.dict.bo.DictAoeBo.class), BizLogCodes.EDIT);
        assertBizLog(DictCtrl.class.getMethod("switchFrozen", org.pkaq.core.mvc.bo.SingleArray.class),
                BizLogCodes.UPDATE);
        assertTrue(DictCtrl.class.getMethod("delDict", Long.class).isAnnotationPresent(Deprecated.class));
    }

    @Test
    void tenantPackageSwitchIsTransactional() throws Exception {
        Method method = TenantPackageService.class.getMethod(
                "switchFrozen", org.pkaq.core.mvc.bo.SingleArray.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertNotNull(transactional);
        assertArrayEquals(new Class<?>[]{Exception.class}, transactional.rollbackFor());
    }

    @Test
    void moduleAndTenantMultiTableWritesAreTransactional() throws Exception {
        assertTransactional(ModuleService.class.getMethod("deleteModule", java.util.Set.class));
        assertTransactional(ModuleService.class.getMethod("editModule", org.pkaq.sys.module.bo.ModuleAoeBo.class));
        assertTransactional(ModuleService.class.getMethod("sortModule", org.pkaq.sys.module.bo.ModuleSortBo.class));
        assertTransactional(ModuleService.class.getMethod(
                "switchFrozen", org.pkaq.sys.module.bo.ModuleFrozenBo.class));
        assertTransactional(TenantService.class.getMethod("delete", java.util.Set.class));
        assertTransactional(TenantService.class.getMethod("switchFrozen", org.pkaq.core.mvc.bo.SingleArray.class));
    }

    private void assertPostMapping(Method method, String path) {
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{path}, mapping.value());
    }

    private void assertBizLog(Method method, BizLogCodes operateType) {
        BizLog bizLog = method.getAnnotation(BizLog.class);
        assertNotNull(bizLog);
        assertEquals(operateType, bizLog.operateType());
    }

    private void assertBizLog(Method method, BizLogCodes operateType, String arg) {
        assertBizLog(method, operateType);
        assertArrayEquals(new String[]{arg}, method.getAnnotation(BizLog.class).args());
    }

    private void assertTransactional(Method method) {
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertNotNull(transactional);
        assertArrayEquals(new Class<?>[]{Exception.class}, transactional.rollbackFor());
    }
}
