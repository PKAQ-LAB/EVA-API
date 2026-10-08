import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 开发库账号拆分工具，先检查，再在单事务中备份并执行版本化 SQL。
 * 不输出凭据，不连接生产配置，不代替生产部署迁移流程。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public final class AccountProfileMigration {

    /**
     * 检查开发库，或执行已经过隔离测试的账号拆分迁移。
     *
     * @param arguments inspect 或 migrate
     * @throws Exception 文件、连接或迁移失败时终止
     */
    public static void main(String[] arguments) throws Exception {
        if (1 != arguments.length || !List.of("inspect", "migrate").contains(arguments[0])) {
            throw new IllegalArgumentException("必须显式指定 inspect 或 migrate");
        }
        Path repository = Path.of("").toAbsolutePath();
        String yaml = Files.readString(repository.resolve("web-booter/src/main/resources/application-dev.yaml"));
        String datasource = yaml.substring(yaml.indexOf("  datasource:"));
        String url = value(datasource, "url").replace("jdbc:p6spy:", "jdbc:");
        if (!url.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("开发配置不是 PostgreSQL，禁止迁移");
        }
        Properties properties = new Properties();
        properties.setProperty("user", value(datasource, "username"));
        properties.setProperty("password", value(datasource, "password"));
        properties.setProperty("connectTimeout", "10");
        properties.setProperty("socketTimeout", "120");
        try (Connection connection = DriverManager.getConnection(url, properties);
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.execute("SET LOCAL lock_timeout = '5s'");
            List<String> schemas = new ArrayList<>();
            try (ResultSet result = statement.executeQuery("SELECT current_database(), current_schema()")) {
                result.next();
                String schema = result.getString(2);
                schemas.add(schema);
                System.out.println("development database=" + result.getString(1) + ", schema=" + schema);
            }
            String core = quoted(schemas.getFirst());
            if (exists(statement, schemas.getFirst(), "sys_tenant")) {
                try (ResultSet result = statement.executeQuery("SELECT DISTINCT SCHEMA_NAME FROM " + core
                        + ".SYS_TENANT WHERE SCHEMA_NAME IS NOT NULL AND to_regnamespace(SCHEMA_NAME) IS NOT NULL")) {
                    while (result.next()) {
                        if (!schemas.contains(result.getString(1))) {
                            schemas.add(result.getString(1));
                        }
                    }
                }
            }
            for (String schema : schemas) {
                String table = exists(statement, schema, "sys_user") ? "SYS_USER" : "SYS_ACCOUNT";
                System.out.println(schema + ": table=" + table + ", rows="
                        + count(statement, quoted(schema) + "." + table));
            }
            if ("inspect".equals(arguments[0])) {
                connection.rollback();
                return;
            }
            boolean hasLegacyAccounts = false;
            for (String schema : schemas) {
                hasLegacyAccounts |= exists(statement, schema, "sys_user");
            }
            if (!hasLegacyAccounts) {
                connection.rollback();
                System.out.println("account migration already applied; no database changes");
                return;
            }
            String backup = "eva_account_backup_" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            statement.execute("CREATE SCHEMA " + quoted(backup));
            statement.execute("REVOKE ALL ON SCHEMA " + quoted(backup) + " FROM PUBLIC");
            for (String schema : schemas) {
                if (exists(statement, schema, "sys_user")) {
                    String source = quoted(schema) + ".SYS_USER";
                    String snapshot = quoted(backup) + "." + quoted(schema);
                    statement.execute("LOCK TABLE " + source + " IN ACCESS EXCLUSIVE MODE");
                    statement.execute("CREATE TABLE " + snapshot + " (LIKE " + source + " INCLUDING ALL)");
                    statement.execute("INSERT INTO " + snapshot + " SELECT * FROM " + source);
                    statement.execute("REVOKE ALL ON TABLE " + snapshot + " FROM PUBLIC");
                    if (count(statement, source) != count(statement, snapshot)) {
                        throw new IllegalStateException("备份行数校验失败，事务回滚");
                    }
                }
            }
            statement.execute(Files.readString(repository.resolve(
                    "web-booter/src/main/resources/db/migration/V12__ACCOUNT_PROFILE_SPLIT.sql")));
            for (String schema : schemas) {
                if (exists(statement, schema, "sys_user")
                        || !exists(statement, schema, "sys_account_profile")) {
                    throw new IllegalStateException("结构校验失败，事务回滚");
                }
                if (exists(statement, backup, schema)) {
                    String snapshot = quoted(backup) + "." + quoted(schema);
                    String accounts = quoted(schema) + ".SYS_ACCOUNT";
                    String profiles = quoted(schema) + ".SYS_ACCOUNT_PROFILE";
                    try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + snapshot + " old"
                            + " JOIN " + accounts + " account_row ON account_row.ID = old.ID"
                            + " JOIN " + profiles + " profile ON profile.ACCOUNT_ID = old.ID"
                            + " WHERE to_jsonb(account_row) = (to_jsonb(old) - 'code' - 'name' - 'dept_id')"
                            + " AND profile.CODE IS NOT DISTINCT FROM old.CODE"
                            + " AND profile.NAME IS NOT DISTINCT FROM old.NAME"
                            + " AND profile.DEPT_ID IS NOT DISTINCT FROM old.DEPT_ID")) {
                        result.next();
                        long matchingRows = result.getLong(1);
                        result.close();
                        if (matchingRows != count(statement, snapshot)) {
                            throw new IllegalStateException("账号及资料逐值校验失败，事务回滚");
                        }
                    }
                }
                System.out.println(schema + ": accounts=" + count(statement, quoted(schema) + ".SYS_ACCOUNT")
                        + ", profiles=" + count(statement, quoted(schema) + ".SYS_ACCOUNT_PROFILE"));
            }
            connection.commit();
            System.out.println("migration committed; restricted backup schema=" + backup);
        } catch (java.sql.SQLException exception) {
            // 不打印可能包含连接信息或请求值的数据库异常明细。
            throw new IllegalStateException("数据库操作失败，SQLState=" + exception.getSQLState());
        }
    }

    private static String value(String yaml, String key) {
        var matcher = Pattern.compile("(?m)^    " + key + ":\\s*([^\\r\\n]+)").matcher(yaml);
        if (!matcher.find()) {
            throw new IllegalStateException("开发数据源配置缺少字段：" + key);
        }
        String value = matcher.group(1).trim();
        if (value.startsWith("${")) {
            throw new IllegalStateException("数据源使用环境变量，请通过正式部署迁移工具执行");
        }
        return value;
    }

    private static String quoted(String name) {
        if (!name.matches("[a-z][a-z0-9_]{0,62}")) {
            throw new IllegalArgumentException("数据库标识符非法");
        }
        return '"' + name + '"';
    }

    private static boolean exists(Statement statement, String schema, String table) throws Exception {
        try (ResultSet result = statement.executeQuery("SELECT to_regclass('" + quoted(schema)
                + "." + table + "') IS NOT NULL")) {
            result.next();
            return result.getBoolean(1);
        }
    }

    private static long count(Statement statement, String relation) throws Exception {
        try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + relation)) {
            result.next();
            return result.getLong(1);
        }
    }
}
