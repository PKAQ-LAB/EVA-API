package io.nerv.security.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import io.nerv.properties.EvaConfig;
import io.nerv.security.util.SecurityUtil;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 数据权限拦截插件
 */
@Slf4j
@Accessors(chain = true)
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PermissionInterceptor extends AbstractSqlParserHandler implements Interceptor {

    private SecurityUtil securityUtil;

    public PermissionInterceptor(SecurityUtil securityUtil) {
        super();
        this.securityUtil = securityUtil;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // SQL 解析
        this.sqlParser(metaObject);

        // 先判断是不是SELECT操作  (2019-04-10 00:37:31 跳过存储过程)
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (SqlCommandType.SELECT != mappedStatement.getSqlCommandType()
                || StatementType.CALLABLE == mappedStatement.getStatementType()) {
            return invocation.proceed();
        }
        // 获得原始sql
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String originalSql = boundSql.getSql();

        Select select = (Select) CCJSqlParserUtil.parse(originalSql);
        SelectBody selectBody = select.getSelectBody();
        PlainSelect plainSelect = (PlainSelect)selectBody;
        if (securityUtil.getAuthentication() != null){
            securityUtil.getAuthentication().getAuthorities().stream().forEach(item -> {
                log.error("当前请求需要的权限是 -----------------> " + item.getAuthority());
            });

            StringBuilder sb = new StringBuilder();
            sb.append("1 = 1");
            if (null != plainSelect.getWhere() && StrUtil.isNotBlank(plainSelect.getWhere().toString())){
                sb.append(" and ");
                sb.append(plainSelect.getWhere());
            }

            // 拼接权限条件
            Expression where = CCJSqlParserUtil.parseCondExpression(sb.toString());
            ((PlainSelect) selectBody).setWhere(where);

            List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());
            log.error(select.toString());
            log.error(mappings.toString());
            metaObject.setValue("delegate.boundSql.sql", select.toString());
            metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
