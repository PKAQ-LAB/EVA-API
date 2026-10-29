package org.pkaq.test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 在合成实例生命周期内采集绑定独立验证RUN的只读JDBC技术证据。
 *
 * @author PKAQ
 * @date 2026-10-28
 */
public final class PostgresInstanceEvidence {
    private static final String VERSION = "eva-pg-jdbc-v1";
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final Map<String, String> LABELS = Map.of(
            "menu-scope", "org.pkaq.sys.module.AuthorizedMenuPostgresTest",
            "menu-state", "org.pkaq.sys.module.AuthorizedMenuPostgresTest",
            "menu-tenant", "org.pkaq.sys.module.AuthorizedMenuPostgresTest",
            "menu-identity", "org.pkaq.sys.module.AuthorizedMenuPostgresTest",
            "mode-provision", "org.pkaq.core.mybatis.tenant.TenantSchemaModePostgresTest",
            "mode-route", "org.pkaq.core.mybatis.tenant.TenantSchemaModePostgresTest",
            "advisor-order", "org.pkaq.core.mybatis.tenant.TenantSchemaAdvisorOrderPostgresTest",
            "role-isolation", "org.pkaq.sys.role.service.RoleSchemaIsolationPostgresTest");
    private static final Set<String> FIELDS = Set.of("version", "nonce", "testClass", "label", "dataDirectory",
            "serverAddress", "serverPort", "listenAddresses", "serverVersionNum", "postmasterStart");
    private static final String SELECT = """
            SELECT pg_catalog.host(pg_catalog.inet_server_addr()), pg_catalog.inet_server_port(),
                   pg_catalog.current_setting('listen_addresses'), pg_catalog.current_setting('data_directory'),
                   pg_catalog.current_setting('server_version_num'), pg_catalog.pg_postmaster_start_time()
            """;

    private PostgresInstanceEvidence() {
    }

    /**
     * 从当前fixture的自有数据源读取技术事实，断言成功后独占发布一条实例证据。
     *
     * @param dataSource 当前合成实例已有的数据源
     * @param label 固定生命周期标签
     * @throws Exception 连接、断言、RUN绑定或独占发布失败
     */
    public static void capture(DataSource dataSource, String label) throws Exception {
        require(LABELS.containsKey(label), "未知合成实例标签");
        require(VERSION.equals(property("pgJdbc.version")), "证据协议版本不符");
        String nonce = property("pgJdbc.nonce");
        require(nonce.matches("[0-9a-f]{64}"), "证据nonce格式不符");
        Path run = Path.of(property("pgJdbc.run")).toRealPath();
        require(run.getFileName().toString().matches("\\d{8}T\\d{6}Z-[0-9a-f]{8}"), "RUN名称不符");
        Path evidence = Path.of(property("pgJdbc.evidence")).toRealPath();
        require(evidence.equals(run.resolve("pg/jdbc-evidence").toRealPath()) && evidence.startsWith(run),
                "证据目录未绑定RUN");
        Path temporary = run.resolve("pg/tmp").toRealPath();
        require(temporary.startsWith(run) && !temporary.equals(run)
                && temporary.equals(Path.of(property("java.io.tmpdir")).toRealPath()), "临时目录未绑定RUN");
        verifyContext(run, evidence, nonce);

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("version", VERSION);
        record.put("nonce", nonce);
        record.put("testClass", LABELS.get(label));
        record.put("label", label);
        // 单一自有连接只执行技术SELECT，不改变测试事务或search_path。
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(SELECT)) {
                require(result.next(), "缺少服务器技术事实");
                String address = result.getString(1);
                int port = result.getInt(2);
                require(!result.wasNull() && 0 < port && port <= 65535, "服务器端口无效");
                String listen = result.getString(3);
                require("127.0.0.1".equals(address) && "127.0.0.1".equals(listen), "监听地址越界");
                String directory = result.getString(4);
                require(null != directory && !directory.isBlank(), "缺少服务器数据目录");
                Path data = Path.of(directory).toRealPath();
                require(data.startsWith(temporary) && !data.equals(temporary), "服务器数据目录越界");
                require("140022".equals(result.getString(5)), "服务器版本不符");
                var started = result.getTimestamp(6);
                require(null != started, "缺少服务器启动时间");
                record.put("dataDirectory", data.toString());
                record.put("serverAddress", address);
                record.put("serverPort", port);
                record.put("listenAddresses", listen);
                record.put("serverVersionNum", 140022);
                record.put("postmasterStart", started.toInstant().toString());
                require(!result.next(), "服务器技术事实行数不符");
            }
        }
        verifyExisting(evidence, temporary, nonce, record);
        publish(evidence, label, JSON.writeValueAsBytes(record));
    }

    private static void verifyContext(Path run, Path evidence, String nonce) throws Exception {
        Path file = run.resolve("pg/jdbc-context.json");
        require(!Files.isSymbolicLink(file) && file.toRealPath().startsWith(run), "证据上下文路径越界");
        byte[] bytes = Files.readAllBytes(file);
        JsonNode seals = JSON.readTree(run.resolve("sealed-control-files.json").toFile());
        String digest = HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        require(digest.equals(seals.path("pg/jdbc-context.json").asText()), "证据上下文未封存");
        JsonNode context = JSON.readTree(bytes);
        require(keys(context).equals(Set.of("version", "nonce", "runRoot", "evidenceDirectory", "expectedLabels")),
                "证据上下文字段不符");
        require(VERSION.equals(text(context, "version")) && nonce.equals(text(context, "nonce")), "证据上下文不符");
        require(run.toString().equals(text(context, "runRoot"))
                && evidence.toString().equals(text(context, "evidenceDirectory")), "证据上下文RUN不符");
        JsonNode labels = context.path("expectedLabels");
        require(labels.isObject() && keys(labels).equals(LABELS.keySet()), "预期标签集合不符");
        for (var entry : LABELS.entrySet()) {
            require(entry.getValue().equals(text(labels, entry.getKey())), "预期标签类型不符");
        }
    }

    private static void verifyExisting(Path evidence, Path temporary, String nonce,
                                       Map<String, Object> record) throws Exception {
        Set<String> labels = new HashSet<>();
        Set<String> directories = new HashSet<>();
        Set<String> starts = new HashSet<>();
        try (var entries = Files.list(evidence)) {
            for (Path file : entries.toList()) {
                require(Files.isRegularFile(file) && !Files.isSymbolicLink(file), "证据目录存在额外条目");
                JsonNode previous = JSON.readTree(file.toFile());
                require(keys(previous).equals(FIELDS), "既有证据字段不符");
                String label = text(previous, "label");
                require(LABELS.containsKey(label) && file.getFileName().toString().equals(label + ".json"),
                        "既有证据标签或文件名不符");
                require(VERSION.equals(text(previous, "version")) && nonce.equals(text(previous, "nonce"))
                        && LABELS.get(label).equals(text(previous, "testClass")), "既有证据绑定不符");
                require("127.0.0.1".equals(text(previous, "serverAddress"))
                        && "127.0.0.1".equals(text(previous, "listenAddresses")), "既有证据监听越界");
                require(previous.path("serverPort").isInt() && 0 < previous.path("serverPort").intValue()
                        && previous.path("serverPort").intValue() <= 65535, "既有证据端口无效");
                require(previous.path("serverVersionNum").isInt()
                        && 140022 == previous.path("serverVersionNum").intValue(), "既有证据版本不符");
                Path directory = Path.of(text(previous, "dataDirectory"));
                require(directory.isAbsolute() && directory.equals(directory.normalize())
                        && directory.startsWith(temporary) && !directory.equals(temporary), "既有证据数据目录越界");
                String started = text(previous, "postmasterStart");
                require(Instant.parse(started).toString().equals(started), "既有证据启动时间不符");
                require(labels.add(label) && directories.add(directory.toString())
                        && starts.add(started), "既有证据实例重复");
            }
        }
        require(!labels.contains(record.get("label")) && !directories.contains(record.get("dataDirectory"))
                && !starts.contains(record.get("postmasterStart")), "合成实例重复");
    }

    private static void publish(Path evidence, String label, byte[] bytes) throws Exception {
        Path pending = evidence.resolve(label + "." + UUID.randomUUID() + ".pending");
        try {
            Files.write(pending, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            // 同目录硬链接只创建新名称，原子发布已关闭的完整文件；不支持时失败且禁止覆盖回退。
            Files.createLink(evidence.resolve(label + ".json"), pending);
            Files.delete(pending);
        } catch (Exception | Error failure) {
            try {
                Files.deleteIfExists(pending);
            } catch (Exception | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private static Set<String> keys(JsonNode node) {
        require(null != node && node.isObject(), "证据必须为对象");
        Set<String> result = new HashSet<>();
        node.fieldNames().forEachRemaining(result::add);
        return result;
    }

    private static String text(JsonNode node, String key) {
        require(node.path(key).isTextual() && !node.path(key).textValue().isBlank(), "证据字符串字段不符");
        return node.path(key).textValue();
    }

    private static String property(String name) {
        String value = System.getProperty(name);
        require(null != value && !value.isBlank(), "缺少验证RUN绑定属性");
        return value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
