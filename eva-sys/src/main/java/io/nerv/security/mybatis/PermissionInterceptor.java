package io.nerv.security.mybatis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import io.nerv.security.domain.JwtGrantedAuthority;
import io.nerv.security.domain.JwtUserDetail;
import io.nerv.security.util.SecurityUtil;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.nerv.web.sys.role.mapper.RoleMapper;
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
import org.springframework.security.core.GrantedAuthority;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

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

        // 先判断是不是SELECT操作
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
            // 获得当前请求需要的权限
            List<RoleEntity> roles = securityUtil.getAuthentication()
                                                 .getAuthorities()
                                                 .stream()
                                                 .map(item -> {
                                                     JwtGrantedAuthority jwtGrantedAuthority = (JwtGrantedAuthority) item;
                                                     return jwtGrantedAuthority.getRoleEntity();
                                                 })
                                                 .collect(Collectors.toList());

            roles.forEach(item -> {
                log.error("----------- ################# --------------"+ item);
            });
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

        JwtUserDetail jwtUserDetail = securityUtil.getJwtUser();

        dataPermission.stream().forEach(item -> {
            if (null == item.getDataPermissionType()) return;
            switch (item.getDataPermissionType()){
                // 仅本部门
                case "0001":    var fchildSql = "select id from sys_user_info where dept_id = '"+item.getDataPermissionDeptid()+"'";
                                permissionSql.append(" ( CREATE_BY in ( ");
                                permissionSql.append(fchildSql);
                                permissionSql.append(" ) OR ");
                                permissionSql.append(" MODIFY_BY in ( ");
                                permissionSql.append(fchildSql);
                                permissionSql.append(" ) ) ");
                                break;
                // 本人所属部门及下属部门
                case "0002":    var schildSql = "select id from sys_user_info sui where dept_id in (select so.id  from  sys_organization so  where so.id='"+item.getDataPermissionDeptid()+"' or so.path like '"+item.getDataPermissionDeptid()+"%')";
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
