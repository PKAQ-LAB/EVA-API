package org.pkaq.sys.user.ctrl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.pkaq.core.advice.CommonResponseAdvice;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.mvc.vo.AuthBootstrapResponse;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.dict.service.DictService;
import org.pkaq.sys.module.service.AuthorizedMenuService;
import org.pkaq.sys.module.vo.ModuleMenuResourceVo;
import org.pkaq.sys.module.vo.ModuleMenuVo;
import org.pkaq.sys.user.service.UserService;
import org.pkaq.web.core.utils.ResponseUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 通过实际菜单控制器、CommonResponseAdvice和ResponseUtil验证兼容外层能力。
 *
 * @author PKAQ
 * @date 2026-10-28
 */
class AuthMenuCapabilityTest {
    @Test
    void keepsArrayAndCurrentEnable() throws Exception {
        EvaConfig config = new EvaConfig();
        AuthorizedMenuService menus = mock(AuthorizedMenuService.class);
        AuthCtrl controller = new AuthCtrl(mock(DictService.class), menus, mock(UserService.class), config);
        ModuleMenuResourceVo resource = new ModuleMenuResourceVo();
        resource.setId(11L);
        resource.setResourceType("POST");
        resource.setResourceUrl("/sys/organization/edit");
        ModuleMenuVo menu = new ModuleMenuVo();
        menu.setId(1L);
        menu.setRouteUrl("/sys/organization");
        menu.setResources(List.of(resource));
        when(menus.fetchCurrentMenus()).thenReturn(List.of(menu));
        for (boolean enabled : new boolean[] {true, false}) {
            config.getResourcePermission().setEnable(enabled);
            config.getResourcePermission().setStrict(!enabled);
            Response<Object> body = controller.fetchMenus();
            assertInstanceOf(AuthBootstrapResponse.class, body);
            JsonNode json = serialize(body);
            assertTrue(json.path("resourcePermissionEnabled").isBoolean());
            assertEquals(enabled, json.path("resourcePermissionEnabled").booleanValue());
            assertTrue(json.path("data").isArray());
            assertEquals(1, json.path("data").size());
            assertEquals("/sys/organization", json.path("data").path(0).path("routeUrl").textValue());
            JsonNode grant = json.path("data").path(0).path("resources").path(0);
            assertEquals("POST", grant.path("resourceType").textValue());
            assertEquals("/sys/organization/edit", grant.path("resourceUrl").textValue());
            assertEquals("0000", json.path("code").textValue());
            assertEquals("合成响应消息", json.path("message").textValue());
            assertFalse(json.path("data").path(0).has("resourcePermissionEnabled"));
        }
    }

    @Test
    void rejectsFailedBootstrap() throws Exception {
        EvaConfig config = new EvaConfig();
        AuthorizedMenuService menus = mock(AuthorizedMenuService.class);
        AuthCtrl controller = new AuthCtrl(mock(DictService.class), menus, mock(UserService.class), config);
        when(menus.fetchCurrentMenus()).thenReturn(List.of());
        JsonNode empty = serialize(controller.fetchMenus());
        assertTrue(empty.path("resourcePermissionEnabled").isBoolean());
        assertFalse(empty.path("resourcePermissionEnabled").booleanValue());
        assertTrue(empty.path("data").isArray());
        assertEquals(0, empty.path("data").size());
        when(menus.fetchCurrentMenus()).thenThrow(new SecurityException("合成初始化失败"));
        JsonNode failed = serialize(controller.fetchMenus());
        assertFalse(failed.path("success").booleanValue());
        assertFalse(failed.has("resourcePermissionEnabled"));
    }

    private JsonNode serialize(Response<?> body) throws Exception {
        I18NHelper messages = mock(I18NHelper.class);
        when(messages.getMessage(anyString(), nullable(Object[].class), anyString()))
                .thenReturn("合成响应消息");
        CommonResponseAdvice advice = new CommonResponseAdvice(messages);
        MockHttpServletResponse output = new MockHttpServletResponse();
        Object advised = advice.beforeBodyWrite(body,
                new MethodParameter(AuthCtrl.class.getMethod("fetchMenus"), -1), MediaType.APPLICATION_JSON,
                Object.class, new ServletServerHttpRequest(new MockHttpServletRequest()),
                new ServletServerHttpResponse(output));
        assertSame(body, advised);
        ResponseUtil.write(output, (Response<?>) advised);
        return new ObjectMapper().readTree(output.getContentAsString());
    }
}
