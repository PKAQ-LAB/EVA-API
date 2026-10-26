package org.pkaq.core.log.pointcut;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pkaq.core.advice.ExceptionInfo;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.BusinessLogRepository;
import org.pkaq.core.log.base.ErrorIncident;
import org.pkaq.core.log.base.ErrorIncidentRepository;
import org.pkaq.core.log.events.BizLogEvent;
import org.pkaq.core.log.events.BusinessLogEventHandler;
import org.pkaq.core.log.events.ErrorIncidentEventHandler;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 业务日志描述与最终写入字段的合成凭据回归测试。
 *
 * @author PKAQ
 * @date 2026-10-19
 */
class BizLogAdviceTest {
    private static final String OLD_CANARY = "synthetic-old-canary";
    private static final String NEW_CANARY = "synthetic-new-canary";
    private static final String TOKEN_CANARY = "synthetic-token-canary";

    @Test
    void hidesLinebreakKeysAtBusinessWriterIncludingNestedEncodedSuccessFailureAndRollback() throws Throwable {
        for (String separator : List.of("\n", "\r", "\r\n", "\u0085", "\u2028", "\u2029", "\u000B", "\f")) {
            Map<String, Object> credentials = Map.of("token" + separator, OLD_CANARY,
                    separator + "newPassword", List.of(NEW_CANARY, TOKEN_CANARY));
            Map<String, Object> payload = Map.of("nested", credentials, "encoded", JsonUtil.toJson(credentials));
            AuditCommand command = new AuditCommand(31L, OLD_CANARY, NEW_CANARY, TOKEN_CANARY,
                    payload, JsonUtil.toJson(payload));
            Fixture fixture = fixture("textField", command);
            when(fixture.joinPoint().proceed()).thenReturn(payload);

            fixture.advice().around(fixture.joinPoint());

            BizLogEntity saved = saved(fixture);
            assertSafe(saved);
            assertTrue(saved.getDescription().contains("31"));
            assertStructuredTextHidden(JsonUtil.toJson(payload));
        }
    }

    @Test
    void hidesLinebreakKeysAtFinalErrorIncidentWriter() {
        for (String separator : List.of("\n", "\r", "\r\n", "\u0085", "\u2028", "\u2029", "\u000B", "\f")) {
            Map<String, Object> credentials = Map.of("token" + separator, TOKEN_CANARY,
                    separator + "originPassword", List.of(OLD_CANARY, NEW_CANARY));
            String payload = JsonUtil.toJson(Map.of("nested", credentials, "encoded", JsonUtil.toJson(credentials)));
            ErrorIncidentRepository repository = mock(ErrorIncidentRepository.class);
            ExceptionInfo info = new ExceptionInfo().setClassName("SyntheticTarget").setMethod("update")
                    .setExceptionType("IllegalStateException").setParams(payload).setSummary(payload).setTenantId(44L);

            new ErrorIncidentEventHandler(repository).onExceptionEvent(info);

            ArgumentCaptor<ErrorIncident> captor = ArgumentCaptor.forClass(ErrorIncident.class);
            verify(repository).save(captor.capture());
            String allFields = JsonUtil.toJson(captor.getValue());
            assertFalse(allFields.contains(OLD_CANARY));
            assertFalse(allFields.contains(NEW_CANARY));
            assertFalse(allFields.contains(TOKEN_CANARY));
            assertEquals(44L, captor.getValue().getTenantId());
        }
    }

    @Test
    void hidesPrefixedArrayAtFinalWriterOnSuccessFailureAndRollback() throws Throwable {
        assertStructuredTextHidden("prefix {\"token\":[\"" + OLD_CANARY + "\",\"" + NEW_CANARY + "\"]}");
    }

    @Test
    void hidesEscapedKeyAtFinalWriterOnSuccessFailureAndRollback() throws Throwable {
        assertStructuredTextHidden("prefix {\"pass" + "\\u0077ord\":\"" + TOKEN_CANARY + "\"}");
    }

    @Test
    void hidesMalformedArrayAtFinalWriterOnSuccessFailureAndRollback() throws Throwable {
        assertStructuredTextHidden("{\"token\":[\"" + OLD_CANARY + "\",\"" + NEW_CANARY + "\"]");
    }

    @Test
    void hidesValidJsonWithUnsafeNestedTextAtFinalWriterOnSuccessFailureAndRollback() throws Throwable {
        assertStructuredTextHidden(JsonUtil.toJson(Map.of("remark",
                "prefix {\"token\":[\"" + OLD_CANARY + "\",\"" + NEW_CANARY + "\"]}")));
    }

    @Test
    void hidesStructuredFallbackInFinalErrorIncidentFields() {
        String prefixed = "prefix {\"token\":[\"" + OLD_CANARY + "\",\"" + NEW_CANARY + "\"]}";
        for (String text : List.of(prefixed,
                "prefix {\"pass" + "\\u0077ord\":\"" + TOKEN_CANARY + "\"}",
                "{\"token\":[\"" + OLD_CANARY + "\",\"" + NEW_CANARY + "\"]",
                JsonUtil.toJson(Map.of("remark", prefixed)))) {
            ErrorIncidentRepository repository = mock(ErrorIncidentRepository.class);
            ExceptionInfo info = new ExceptionInfo().setClassName("SyntheticTarget").setMethod("update")
                    .setExceptionType("IllegalStateException").setSummary(text)
                    .setParams(JsonUtil.toJson(Map.of("remark", text))).setTenantId(44L);

            new ErrorIncidentEventHandler(repository).onExceptionEvent(info);

            ArgumentCaptor<ErrorIncident> captor = ArgumentCaptor.forClass(ErrorIncident.class);
            verify(repository).save(captor.capture());
            String allFields = JsonUtil.toJson(captor.getValue());
            assertFalse(allFields.contains(OLD_CANARY));
            assertFalse(allFields.contains(NEW_CANARY));
            assertFalse(allFields.contains(TOKEN_CANARY));
            assertEquals(44L, captor.getValue().getTenantId());
        }
    }

    private void assertStructuredTextHidden(String text) throws Throwable {
        for (String outcome : List.of("success", "failure", "rollback")) {
            AuditCommand command = new AuditCommand(31L, OLD_CANARY, NEW_CANARY, TOKEN_CANARY,
                    Map.of("remark", text), text);
            Fixture fixture = fixture("textField", command, "rollback".equals(outcome));
            IllegalStateException failure = new IllegalStateException(text);
            if ("failure".equals(outcome)) {
                when(fixture.joinPoint().proceed()).thenThrow(failure);
                assertTrue(assertThrows(IllegalStateException.class,
                        () -> fixture.advice().around(fixture.joinPoint())) == failure);
            } else {
                when(fixture.joinPoint().proceed()).thenReturn(Map.of("remark", text));
                fixture.advice().around(fixture.joinPoint());
            }
            BizLogEntity saved = saved(fixture);
            assertSafe(saved);
            assertTrue(saved.getDescription().contains("31"));
            assertEquals("success".equals(outcome), saved.getSuccess());
            if (!"success".equals(outcome)) {
                assertTrue(saved.getDescription().startsWith(BusinessLogEventHandler.FAILURE_PREFIX));
            }
        }
    }

    @Test
    void projectsWholeBeanBeforeFormattingAndSavesSanitizedResponse() throws Throwable {
        AuditCommand command = command(17L);
        assertTrue(MessageFormat.format("更新[{0}]", command).contains(OLD_CANARY));
        Fixture fixture = fixture("wholeBean", command);
        Map<String, Object> response = Map.of("id", 17L, "accessToken", TOKEN_CANARY);
        when(fixture.joinPoint().proceed()).thenReturn(response);

        assertTrue(fixture.advice().around(fixture.joinPoint()) == response);

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertEquals(Boolean.TRUE, saved.getSuccess());
        assertEquals("U", saved.getOperateType());
        assertTrue(saved.getDescription().contains("17"));
        assertTrue(saved.getDescription().contains("更新"));
    }

    @Test
    void savesSanitizedFailureWithoutChangingBusinessException() throws Throwable {
        Fixture fixture = fixture("wholeBean", command(18L));
        IllegalStateException failure = new IllegalStateException(TOKEN_CANARY);
        when(fixture.joinPoint().proceed()).thenThrow(failure);

        assertTrue(assertThrows(IllegalStateException.class,
                () -> fixture.advice().around(fixture.joinPoint())) == failure);

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertEquals(Boolean.FALSE, saved.getSuccess());
        assertTrue(saved.getDescription().startsWith(BusinessLogEventHandler.FAILURE_PREFIX));
        assertTrue(saved.getDescription().contains("18"));
    }

    @Test
    void masksExplicitCredentialFieldsAndKeepsIndependentTargetIdentifier() throws Throwable {
        Fixture fixture = fixture("secretField", command(19L));
        when(fixture.joinPoint().proceed()).thenReturn(Map.of("token", TOKEN_CANARY));

        fixture.advice().around(fixture.joinPoint());

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertEquals("19", saved.getBId());
        assertTrue(saved.getDescription().contains("******"));
        assertTrue(saved.getDescription().contains("19"));
    }

    @Test
    void projectsResultObjectBeforeFormatting() throws Throwable {
        Fixture fixture = fixture("resultBean", command(20L));
        when(fixture.joinPoint().proceed()).thenReturn(command(21L));

        fixture.advice().around(fixture.joinPoint());

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertTrue(saved.getDescription().contains("21"));
    }

    @Test
    void masksNestedAndEncodedCredentialResponsesAtWriterBoundary() throws Throwable {
        Fixture fixture = fixture("resultBean", command(27L));
        when(fixture.joinPoint().proceed()).thenReturn(Map.of("id", 28L,
                "nested", Map.of("refreshToken", List.of(TOKEN_CANARY, NEW_CANARY)),
                "payload", JsonUtil.toJson(Map.of("originPassword", OLD_CANARY))));

        fixture.advice().around(fixture.joinPoint());

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertEquals("28", String.valueOf(JsonUtil.toMap(saved.getResponse()).get("id")));
        assertTrue(saved.getDescription().contains("28"));
    }

    @Test
    void handlesNullBusinessIdentifierOnFailure() throws Throwable {
        Fixture fixture = fixture("secretField", command(null));
        when(fixture.joinPoint().proceed()).thenThrow(new IllegalArgumentException("synthetic failure"));

        assertThrows(IllegalArgumentException.class, () -> fixture.advice().around(fixture.joinPoint()));

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertEquals(Boolean.FALSE, saved.getSuccess());
    }

    @Test
    void preservesCreateAndUpdateClassification() throws Throwable {
        Fixture create = fixture("edit", command(null));
        create.advice().around(create.joinPoint());
        assertEquals("C", saved(create).getOperateType());

        Fixture update = fixture("edit", command(22L));
        update.advice().around(update.joinPoint());
        assertEquals("U", saved(update).getOperateType());
    }

    @Test
    void capturesTenantAndActorBeforeAsyncWriter() throws Throwable {
        Fixture fixture = fixture("wholeBean", command(23L));
        ThreadUser user = new ThreadUser().setTenantId(44L).setUserId(55L).setName("audit-actor");
        ThreadUserHelper.runWithUser(user, () -> {
            try {
                fixture.advice().around(fixture.joinPoint());
            } catch (Throwable failure) {
                throw new AssertionError("合成审计操作失败", failure);
            }
        });

        BizLogEntity saved = saved(fixture);
        assertSafe(saved);
        assertEquals(44L, saved.getTenantId());
        assertEquals(55L, saved.getUserId());
        assertEquals("audit-actor", saved.getOperator());
    }

    @Test
    void rollbackWriterMarksPreviouslySuccessfulOperationFailed() {
        BusinessLogRepository repository = mock(BusinessLogRepository.class);
        BizLogEntity event = new BizLogEntity().setSuccess(Boolean.TRUE).setDescription("更新目标[24]");

        new BusinessLogEventHandler(repository).listenerRollback(new BizLogEvent(event));

        ArgumentCaptor<BizLogEntity> captor = ArgumentCaptor.forClass(BizLogEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getSuccess());
        assertTrue(captor.getValue().getDescription().startsWith(BusinessLogEventHandler.FAILURE_PREFIX));
        assertSafe(captor.getValue());
    }

    private AuditCommand command(Long id) {
        return new AuditCommand(id, OLD_CANARY, NEW_CANARY, TOKEN_CANARY, Map.of(
                "nested", Map.of("token", List.of(TOKEN_CANARY, NEW_CANARY)),
                "payload", JsonUtil.toJson(Map.of("password", OLD_CANARY))), "普通备注");
    }

    private Fixture fixture(String methodName, AuditCommand command) throws Throwable {
        return fixture(methodName, command, false);
    }

    private Fixture fixture(String methodName, AuditCommand command, boolean rollback) throws Throwable {
        BusinessLogRepository repository = mock(BusinessLogRepository.class);
        BusinessLogEventHandler handler = new BusinessLogEventHandler(repository);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doAnswer(invocation -> {
            if (rollback) {
                handler.listenerRollback(invocation.getArgument(0, BizLogEvent.class));
            } else {
                handler.listenerCommit(invocation.getArgument(0, BizLogEvent.class));
            }
            return null;
        }).when(publisher).publishEvent(any(BizLogEvent.class));
        Method method = AuditTarget.class.getMethod(methodName, AuditCommand.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getName()).thenReturn(methodName);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(new AuditTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] {command});
        return new Fixture(new BizLogAdvice(publisher, mock(I18NHelper.class)), joinPoint, repository);
    }

    private BizLogEntity saved(Fixture fixture) {
        ArgumentCaptor<BizLogEntity> captor = ArgumentCaptor.forClass(BizLogEntity.class);
        verify(fixture.repository()).save(captor.capture());
        return captor.getValue();
    }

    private void assertSafe(BizLogEntity saved) {
        String allFields = JsonUtil.toJson(saved);
        assertFalse(allFields.contains(OLD_CANARY), "日志写入不得携带原密码");
        assertFalse(allFields.contains(NEW_CANARY), "日志写入不得携带新密码");
        assertFalse(allFields.contains(TOKEN_CANARY), "日志写入不得携带令牌");
        if (saved.getParams() != null) {
            assertTrue(JsonUtil.readTree(saved.getParams()) != null);
        }
        if (saved.getResponse() != null) {
            assertTrue(JsonUtil.readTree(saved.getResponse()) != null);
        }
    }

    private record Fixture(BizLogAdvice advice, ProceedingJoinPoint joinPoint, BusinessLogRepository repository) {
    }

    private record AuditCommand(Long id, String originPassword, String newPassword,
                                String accessToken, Map<String, Object> details, String remark) {
    }

    /**
     * 提供真实注解与描述模板的隔离目标。
     *
     * @author PKAQ
     * @date 2026-10-19
     */
    public static class AuditTarget {
        /** 执行包含不可信文本的描述。 */
        @BizLog(operateType = BizLogCodes.UPDATE, description = "更新[{0}][{1}]",
                args = {"param:0.id", "param:0.remark"})
        public void textField(AuditCommand command) {
        }

        /** 执行整体参数日志。 */
        @BizLog(operateType = BizLogCodes.UPDATE, description = "更新[{0}]", args = {"param:0"})
        public void wholeBean(AuditCommand command) {
        }

        /** 执行显式凭据字段日志。 */
        @BizLog(operateType = BizLogCodes.UPDATE, description = "更新[{0}][{1}]",
                args = {"param:0.newPassword", "param:0.id"}, bizId = "param:0.id")
        public void secretField(AuditCommand command) {
        }

        /** 执行整体响应日志。 */
        @BizLog(operateType = BizLogCodes.UPDATE, description = "更新[{0}]", args = {"this"})
        public void resultBean(AuditCommand command) {
        }

        /** 执行新增或修改日志。 */
        @BizLog(operateType = BizLogCodes.EDIT, description = "编辑[{0}]", args = {"param:0.id"})
        public void edit(AuditCommand command) {
        }
    }
}
