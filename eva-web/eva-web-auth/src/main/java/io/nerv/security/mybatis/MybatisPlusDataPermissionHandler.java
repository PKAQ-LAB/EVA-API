package io.nerv.security.mybatis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import io.nerv.core.annotation.Ignore;
import io.nerv.core.enums.DataPermissionEnumm;
import io.nerv.core.security.domain.JwtUserDetail;
import io.nerv.core.util.SecurityHelper;
import io.nerv.properties.EvaConfig;
import io.nerv.security.domain.JwtGrantedAuthority;
import io.nerv.web.sys.role.entity.RoleEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限拦截插件
 */
@Slf4j
@Getter
@Setter
public class MybatisPlusDataPermissionHandler implements DataPermissionHandler {
    private EvaConfig evaConfig;

    private SecurityHelper securityHelper;

    public MybatisPlusDataPermissionHandler(SecurityHelper securityHelper) {
        super();
        this.securityHelper = securityHelper;
    }
    /**
     * @param where             原SQL Where 条件表达式
     * @param mappedStatementId Mapper接口方法ID
     * @return
     */
    @SneakyThrows
    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        //  未启用数据权限控制 直接返回
        if (null == evaConfig.getDataPermission() || !evaConfig.getDataPermission().isEnable()) {
            return where;
        }
        // 是否为排除的语句 通过配置文件或@Ignore注解
        if (this.isIgnored(mappedStatementId) || this.isExcluded(mappedStatementId)){
            return where;
        }

        if (securityHelper.getAuthentication() != null && !this.isExcluded(mappedStatementId)) {
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

            Expression permission = CCJSqlParserUtil.parseCondExpression(permissionSQL);

            // 查询当前权限组拥有的数据权限
            // 根据权限拼接查询语句
            if (StrUtil.isNotBlank(permissionSQL)){
                return new AndExpression(where, permission);
            }
        }
        return where;
    }
    /**
     * 判断是否存在忽略注解
     * @param statementId
     * @return
     */
    public boolean isIgnored(String statementId) throws ClassNotFoundException {
        String namespace = statementId;
        String className = namespace.substring(0,namespace.lastIndexOf("."));
        String methedName= namespace.substring(namespace.lastIndexOf(".") + 1);
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
     * @param statementId
     * @return
     */
    public boolean isExcluded(String statementId){
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeStatements();
        return CollUtil.isNotEmpty(excludeTables)
                &&
                excludeTables.stream()
                        .filter(item -> statementId.equals(item))
                        .count() > 0;
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
            if (null == item.getDataPermissionType() || DataPermissionEnumm.ALL.getV().equals(item.getDataPermissionType())) return;
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
}
