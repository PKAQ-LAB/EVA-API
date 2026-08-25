package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.spring.activerecord.Model;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MyBatis-Plus ActiveRecord 适配回归测试。
 *
 * @author PKAQ
 */
class StdActiveServiceTest {

    /**
     * 验证 Spring Boot 4 starter 中的 ActiveRecord 模型可用于通用服务。
     */
    @Test
    void shouldUseSpringActiveRecordModelWithBaseMapper() {
        TestMapper mapper = mock(TestMapper.class);
        TestService service = new TestService();
        TestEntity expected = new TestEntity();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        when(mapper.selectById("1")).thenReturn(expected);

        TestEntity actual = service.getById("1");

        assertSame(expected, actual);
        verify(mapper).selectById("1");
    }

    private static final class TestService extends StdActiveService<TestMapper, TestEntity> {
    }

    private interface TestMapper extends BaseMapper<TestEntity> {
    }

    private static final class TestEntity extends Model<TestEntity> {
        @Override
        public Serializable pkVal() {
            return null;
        }
    }
}
