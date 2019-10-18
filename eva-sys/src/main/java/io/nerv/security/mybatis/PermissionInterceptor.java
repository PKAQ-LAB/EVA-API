package io.nerv.security.mybatis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import io.nerv.core.annotation.Ignore;
import io.nerv.core.security.domain.JwtUserDetail;
import io.nerv.core.util.SecurityHelper;
import io.nerv.properties.EvaConfig;
import io.nerv.security.domain.JwtGrantedAuthority;
import io.nerv.web.sys.role.entity.RoleEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 数据权限拦截插件
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PermissionInterceptor extends AbstractSqlParserHandler implements Interceptor {

    private EvaConfig evaConfig;

    private SecurityHelper securityHelper;

    public PermissionInterceptor(SecurityHelper securityHelper) {
        super();
        this.securityHelper = securityHelper;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //  判断是否启用了数据权限
        if (null != evaConfig.getDataPermission() && !evaConfig.getDataPermission().isEnable()){
            return invocation.proceed();
        }

        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // SQL 解析
        this.sqlParser(metaObject);

        // 先判断是不是SELECT操作
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 是否为排除的语句 通过配置文件或注解
        if (this.isIgnored(mappedStatement) || this.isExcluded(mappedStatement)){
            return invocation.proceed();
        }

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

        if (securityHelper.getAuthentication() != null && !this.isExcluded(select)){
            // 获得当前请求需要的权限
            List<RoleEntity> roles = securityHelper.getAuthentication()
                                                 .getAuthorities()
                                                 .stream()
                                                 .map(item -> {
                                                     JwtGrantedAuthority jwtGrantedAuthority = (JwtGrantedAuthority) item;
                                                     log.debug(" ||| -- 当前用户权限为 -- ||| " + jwtGrantedAuthority.getRoleEntity());
                                                     return jwtGrantedAuthority.getRoleEntity();
                                                 })
                                                 .collect(Collectors.toList());

            // 获得当前请求所需角色的数据权限
            String permissionSQL = this.permissionSql(roles);

            // 查询当前权限组拥有的数据权限
            // 根据权限拼接查询语句
            StringBuilder sb = new StringBuilder();
            sb.append("1 = 1");
            if (StrUtil.isNotBlank(permissionSQL)){
                sb.append(" and ");
                sb.append(permissionSQL);
            }

            if (null != plainSelect.getWhere() && StrUtil.isNotBlank(plainSelect.getWhere().toString())){
                sb.append(" and ");
                sb.append(plainSelect.getWhere());
            }

            // 拼接权限条件
            Expression where = CCJSqlParserUtil.parseCondExpression(sb.toString());
            ((PlainSelect) selectBody).setWhere(where);

            List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());
            metaObject.setValue("delegate.boundSql.sql", select.toString());
            metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
        }
        return invocation.proceed();
    }

    /**
     * 生成权限限制sql
     * @param dataPermission
     * @return
     */
    public String permissionSql(List<RoleEntity> dataPermission){
        StringBuilder permissionSql = new StringBuilder(" ( ");

        JwtUserDetail jwtUserDetail = securityHelper.getJwtUser();

        dataPermission.stream().forEach(item -> {
            if (null == item.getDataPermissionType()) return;
            switch (item.getDataPermissionType()){
                // 仅本部门
                case "0001":    var fchildSql = "select id from sys_user_info where dept_id = '"+jwtUserDetail.getDeptId()+"'";
                                permissionSql.append(" ( CREATE_BY in ( ");
                                permissionSql.append(fchildSql);
                                permissionSql.append(" ) OR ");
                                permissionSql.append(" MODIFY_BY in ( ");
                                permissionSql.append(fchildSql);
                                permissionSql.append(" ) ) ");
                                break;
                // 本人所属部门及下属部门
                case "0002":    var schildSql = "select id from sys_user_info sui where dept_id in (select so.id  from  sys_organization so  where so.id='"+jwtUserDetail.getDeptId()+"' or so.path like '"+jwtUserDetail.getDeptId()+"%')";
                                permissionSql.append(" ( CREATE_BY in ( ");
                                permissionSql.append(schildSql);
                                permissionSql.append(" ) OR ");
                                permissionSql.append(" MODIFY_BY in ( ");
                                permissionSql.append(schildSql);
                                permissionSql.append(" ) ) ");
                                break;
                // 指定部门
                case "0003":    String deptId = Arrays.stream(item.getDataPermissionDeptid().split(","))
                                                      .map(str -> "'"+item+"'")
                                                      .collect(Collectors.joining(","));
                                var tchildSql = "select id from sys_user_info where dept_id in (" +deptId+ ")";
                                permissionSql.append(" ( CREATE_BY in ( ");
                                permissionSql.append(tchildSql);
                                permissionSql.append(" ) OR ");
                                permissionSql.append(" MODIFY_BY in ( ");
                                permissionSql.append(tchildSql);
                                permissionSql.append(" ) ) ");
                                break;
                // 本人创建或修改
                case "0005":    permissionSql.append(" ( ");
                                permissionSql.append("CREATE_BY = '"+jwtUserDetail.getId()+"'");
                                permissionSql.append(" or ");
                                permissionSql.append("MODIFY_BY = '"+jwtUserDetail.getId()+"'");
                                permissionSql.append(" ) ");
                                break;

                default:        permissionSql.append(" 1 = 1 ");
            }
            permissionSql.append(" or ");
        });
        permissionSql.delete(permissionSql.lastIndexOf(" or "), permissionSql.length()-1);
        permissionSql.append(" ) ");

        return permissionSql.toString();
    }

    /**
     * 判断是否存在忽略注解
     * @param mappedStatement
     * @return
     * @throws ClassNotFoundException
     */
    public boolean isIgnored(MappedStatement mappedStatement) throws ClassNotFoundException {
        String namespace = mappedStatement.getId();
        String className = namespace.substring(0,namespace.lastIndexOf("."));
        String methedName= namespace.substring(namespace.lastIndexOf(".") + 1,namespace.length());
        Class clazz = Class.forName(className);
        // 判断类注解
        if (null != clazz && clazz.getAnnotation(Ignore.class) instanceof Ignore){
            return true;
        }

        // 判断方法注解
        Method[] ms = Class.forName(className).getMethods();

        return Arrays.stream(ms).filter(item ->
                                        methedName.equals(item.getName()) && item.getAnnotation(Ignore.class) instanceof Ignore)
                                .count() > 0;
    }
    /**
     * 判断是否为排除不过滤的语句
     * @param mappedStatement
     * @return
     */
    public boolean isExcluded(MappedStatement mappedStatement){
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeStatements();
        String statementId = mappedStatement.getId();
        return CollUtil.isNotEmpty(excludeTables)
               &&
               excludeTables.stream()
                            .filter(item -> statementId.equals(item))
                            .count() > 0;
    }
    /**
     * 判断是否存在被排除得表
     * @param select
     * @return
     */
    public boolean isExcluded(Select select) {
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeStatements();

        if (CollUtil.isEmpty(excludeTables)) return false;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        var tableList = tablesNamesFinder.getTableList(select);

        return tableList.stream().filter(item -> {
            boolean excluded = false;
            for (String et : excludeTables) {
                if (et.equals(item)){
                    excluded = true;
                    break;
                };
            }
            return excluded;
        }).count() > 0;
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
