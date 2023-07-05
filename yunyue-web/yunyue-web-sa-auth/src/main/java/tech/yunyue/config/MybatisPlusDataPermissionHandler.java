package tech.yunyue.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.yunyue.core.annotation.Ignore;
import tech.yunyue.core.enums.DataPermissionEnumm;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 数据权限拦截插件
 */
@Component
public class MybatisPlusDataPermissionHandler implements MultiDataPermissionHandler {
    @Autowired
    private EvaConfig evaConfig;

    public MybatisPlusDataPermissionHandler() {
        super();
    }

    @SneakyThrows
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        //  未启用数据权限控制 直接返回
        if (null == evaConfig.getDataPermission() || !evaConfig.getDataPermission().isEnable()) {
            return null;
        }
        // 是否为排除的语句 通过配置文件或@Ignore注解
        if (this.isIgnored(mappedStatementId) || this.isExcluded(mappedStatementId) || this.isExcluded(table)){
            return null;
        }
        var roles = ThreadUserHelper.getUsetGrantedRoleList();
        // 用户没有角色 不返回数据
        if(CollectionUtil.isEmpty(roles)){
            return CCJSqlParserUtil.parseCondExpression(String.valueOf(Boolean.FALSE));
        }
        // 获得当前请求所需角色的数据权限
        String permissionSQL = this.permissionSql(roles, getAliasColumn(table));
        // 根据权限拼接查询语句
        if (StrUtil.isNotBlank(permissionSQL)){
            return CCJSqlParserUtil.parseCondExpression(permissionSQL);
        }
        return null;
    }

    /**
     * 判断是否存在忽略注解
     * @param statementId
     * @return
     */
    public boolean isIgnored(String statementId) throws ClassNotFoundException {
        var method = getMethod(statementId);;
        return method == null || method.getAnnotation(Ignore.class) != null;
    }
    /**
     * 判断是否为排除不过滤的语句
     * @param statementId
     * @return
     */
    public boolean isExcluded(String statementId){
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeStatements();
        return CollUtil.isNotEmpty(excludeTables)
                && excludeTables.stream().anyMatch(statementId::equals);
    }
    /**
     * 判断是否为排除不过滤的表
     * @param table
     * @return
     */
    public boolean isExcluded(Table table){
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeTables();
        return CollUtil.isNotEmpty(excludeTables)
                && excludeTables.stream().anyMatch(table.getName()::equals);
    }

    /**
     * 生成权限限制sql
     * @param dataPermission
     * @return
     */
    public String permissionSql(List<ThreadUser.GrantedRoles> dataPermission, String tableName){
        StringBuilder permissionSql = new StringBuilder(" ( ");
        AtomicReference<Boolean> isAll = new AtomicReference<>(false);

        var depId = ThreadUserHelper.getOrgId();
        var postId = ThreadUserHelper.getPostId();
        var uId = ThreadUserHelper.getUserId();
        dataPermission.stream().anyMatch(item ->{
            var permissionEnum = DataPermissionEnumm.getByCode(item.getDataPermissionType());
            // 其中一个角色的数据权限是全部 则跳出循环
            if (DataPermissionEnumm.ALL.equals(permissionEnum)) {
                isAll.set(true);
                return true;
            }
            var sql = "";
            switch (permissionEnum){
                //仅本部门
                case DEPT_ONLY_LIMIT -> sql = "ORG_ID = '"+depId+"'";
                //本人所属部门及下属部门
                case DEPT_AND_CHILDREN_LIMIT -> sql = "ORG_ID in (select dp_so.id from sys_organization dp_so where dp_so.id='"+depId+"' or dp_so.path like '%"+depId+"%')";
                //指定部门
                case DEPT_LIMIT ->{
                    String deptId = Arrays.stream(item.getDataPermissionDeptid().split(","))
                        .map(str -> "'"+str+"'")
                        .collect(Collectors.joining(","));
                    sql = "ORG_ID in (" +deptId+ ")";
                }
                //仅本岗位
                case POST_ONLY_LIMIT -> sql = "POST_ID = '"+postId+"'";
                //本人所属岗位及下属岗位
                case POST_AND_CHILDREN_LIMIT -> sql = "POST_ID in (select dp_sp.id from sys_post dp_sp where dp_sp.id='"+postId+"' or dp_sp.path_id like '%"+postId+"%')";
                //指定岗位
                case POST_LIMIT ->{
                    var postIds = Arrays.stream(item.getDataPermissionDeptid().split(","))
                            .map(str -> "'"+str+"'")
                            .collect(Collectors.joining(","));
                    sql = "POST_ID in (" +postIds+ ")";
                }
                //本人创建或修改
                case CREATOR_LIMIT ->
                        sql = " ( " + tableName + "CREATE_BY = '" + uId + "' or " + tableName + "MODIFY_BY = '" + uId + "' ) ";
            }
            if (!permissionEnum.equals(DataPermissionEnumm.CREATOR_LIMIT)) {
                sql = " ( " + tableName + sql + " ) ";
            }
            permissionSql.append(sql).append(" or ");
            return false;
        });
        //存在拥有全部权限的角色 不添加数据权限sql
        if (isAll.get()){
            return "";
        }
        permissionSql.delete(permissionSql.lastIndexOf(" or "), permissionSql.length()-1);
        permissionSql.append(" ) ");

        return permissionSql.toString();
    }

    /**
     * mapper接口类
     */
    public Class getClass(String statementId) throws ClassNotFoundException {
        String className = statementId.substring(0,statementId.lastIndexOf("."));
        return Class.forName(className);
    }

    /**
     * 调用的方法
     */
    public Method getMethod(String statementId) throws ClassNotFoundException {
        Class clazz = getClass(statementId);
        // 判断类注解
        if (null == clazz || clazz.getAnnotation(Ignore.class) != null){
            return null;
        }

        // 判断方法注解
        Method[] ms = clazz.getMethods();
        String methedName= statementId.substring(statementId.lastIndexOf(".") + 1);
        return Arrays.stream(ms).filter(item ->  methedName.equals(item.getName())).findFirst().get();
    }

    /**
     * 租户字段别名设置
     * <p>tenantId 或 tableAlias.tenantId</p>
     *
     * @param table 表对象
     * @return 字段
     */
    protected String getAliasColumn(Table table) {
        return Optional.ofNullable(table.getAlias()).orElse(new Alias(table.getName())).getName() + StrUtil.DOT;
    }
}
