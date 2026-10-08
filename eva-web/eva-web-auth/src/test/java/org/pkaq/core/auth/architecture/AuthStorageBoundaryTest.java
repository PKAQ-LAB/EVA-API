package org.pkaq.core.auth.architecture;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.AuthCodes;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对编译后的类引用验证安全核心与存储适配器的单向依赖。
 *
 * @author Codex
 * @date 2026-10-08
 */
class AuthStorageBoundaryTest {
    private static final List<String> CORE_PACKAGES = List.of(
            "authentication", "authorization", "spi", "openapi/filter", "tenant", "util");
    private static final Set<String> FORBIDDEN_REFERENCES = Set.of(
            "com/baomidou/", "org/apache/ibatis/", "org/pkaq/core/mybatis/",
            "org/pkaq/core/auth/adapter/", "org/pkaq/core/auth/user/",
            "org/pkaq/core/auth/role/", "org/pkaq/core/auth/rbac/");

    /** 核心类的实际字节码不得依赖存储实现或旧持久化模型。 */
    @Test
    void shouldKeepCoreIndependentOfStorage() throws Exception {
        Path root = classesRoot();
        List<String> violations = new ArrayList<>();
        for (String packageName : CORE_PACKAGES) {
            Path directory = root.resolve("org/pkaq/core/auth/" + packageName);
            assertTrue(Files.isDirectory(directory), "未找到目标安全包: " + packageName);
            List<Path> classes;
            try (var files = Files.walk(directory)) {
                classes = files.filter(path -> path.toString().endsWith(".class")).toList();
            }
            assertFalse(classes.isEmpty(), "安全包不能为空: " + packageName);
            for (Path classFile : classes) {
                for (String reference : constantPool(classFile)) {
                    if ("spi".equals(packageName) && (reference.contains("org/pkaq/core/auth/authentication/")
                            || reference.contains("org/pkaq/core/auth/authorization/"))) {
                        violations.add(root.relativize(classFile) + " -> " + reference);
                    }
                    for (String forbidden : FORBIDDEN_REFERENCES) {
                        if (reference.contains(forbidden)) {
                            violations.add(root.relativize(classFile) + " -> " + reference);
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "安全核心引用存储实现: " + violations);
    }

    /** SPI 传输模型不得继承业务实体，也不能携带 SQL 映射注解。 */
    @Test
    void shouldKeepSnapshotsFreeOfEntities() throws Exception {
        Path modelDirectory = classesRoot().resolve("org/pkaq/core/auth/spi/model");
        assertTrue(Files.isDirectory(modelDirectory), "SPI 模型包不存在");
        List<Path> models;
        try (var files = Files.walk(modelDirectory)) {
            models = files.filter(path -> path.toString().endsWith(".class")).toList();
        }
        assertFalse(models.isEmpty(), "SPI 模型不能为空");
        for (Path model : models) {
            List<String> references = constantPool(model);
            assertFalse(references.stream().anyMatch(value -> value.contains("StdEntity")
                            || value.contains("com/baomidou/") || value.contains("org/apache/ibatis/")),
                    "SPI 模型携带持久化实体依赖: " + model.getFileName());
        }
    }

    /** 迁包后的 XML、别名与 Mapper 方法必须能在无数据库条件下装配。 */
    @Test
    void shouldResolveMovedMapperXmlAndAliases() throws Exception {
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAlias("authUser", Class.forName(
                "org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity"));
        configuration.getTypeAliasRegistry().registerAlias("authRole", Class.forName(
                "org.pkaq.core.auth.adapter.mybatis.role.entity.AuthRoleEntity"));
        for (String resource : List.of("mapper/AuthUser.xml", "mapper/AuthRole.xml", "mapper/AuthRoleResource.xml")) {
            try (var input = AuthCodes.class.getClassLoader().getResourceAsStream(resource)) {
                assertTrue(null != input, "Mapper 资源不存在: " + resource);
                new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        assertTrue(configuration.hasStatement(
                "org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthUserMapper.getUserAccount"));
        assertTrue(configuration.hasStatement(
                "org.pkaq.core.auth.adapter.mybatis.role.mapper.AuthRoleMapper.selectByUserId"));
    }

    private Path classesRoot() throws Exception {
        return Path.of(AuthCodes.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private List<String> constantPool(Path classFile) throws IOException {
        List<String> references = new ArrayList<>();
        try (DataInputStream input = new DataInputStream(Files.newInputStream(classFile))) {
            assertTrue(0xCAFEBABE == input.readInt(), "非法字节码文件: " + classFile);
            input.readUnsignedShort();
            input.readUnsignedShort();
            int count = input.readUnsignedShort();
            for (int i = 1; i < count; i++) {
                int tag = input.readUnsignedByte();
                switch (tag) {
                    case 1 -> references.add(input.readUTF());
                    case 3, 4 -> input.skipNBytes(4);
                    case 5, 6 -> {
                        input.skipNBytes(8);
                        i++;
                    }
                    case 7, 8, 16, 19, 20 -> input.skipNBytes(2);
                    case 9, 10, 11, 12, 17, 18 -> input.skipNBytes(4);
                    case 15 -> input.skipNBytes(3);
                    default -> throw new IOException("无法识别字节码常量类型: " + tag);
                }
            }
        }
        return references;
    }
}
