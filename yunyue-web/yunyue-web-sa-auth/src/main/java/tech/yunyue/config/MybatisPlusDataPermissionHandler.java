package tech.yunyue.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.yunyue.core.annotation.DatapermissionTable;
import tech.yunyue.core.annotation.Ignore;
import tech.yunyue.core.enums.DataPermissionEnumm;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 数据权限拦截插件
 */
@Component
public class MybatisPlusDataPermissionHandler implements DataPermissionHandler {
    @Autowired
    private EvaConfig evaConfig;

    public MybatisPlusDataPermissionHandler() {
        super();
    }


    @SneakyThrows
    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        //  未启用数据权限控制 直接返回
        if (null == evaConfig.getDataPermission() || !evaConfig.getDataPermission().isEnable()) {
            return where;
        }
        // 是否为排除的语句 通过配置文件或@Ignore注解
        if (this.isIgnored(mappedStatementId) || this.isExcludedStatementId(mappedStatementId) || this.isExcludedTable(mappedStatementId)){
            return where;
        }
        // 获得当前请求所需角色的数据权限
        String permissionSQL = permissionSql(getDatapermissionTableName(mappedStatementId));
        // 根据权限拼接查询语句
        if (StrUtil.isNotBlank(permissionSQL)){
            return new AndExpression(where, CCJSqlParserUtil.parseCondExpression(permissionSQL));
        }
        return where;
    }


    public static String permissionSql(String tableName){
        var roles = ThreadUserHelper.getUsetGrantedRoleList();
        // 用户没有角色 不返回数据
        if(CollectionUtil.isEmpty(roles)){
            return "false";
        }
        // 表名不为空时 加上.
        if (StringUtils.isNoneBlank(tableName)){
            tableName += StrUtil.DOT;
        }
        return permissionSql(roles, tableName);
    }

    /**
     * @return 判断是否存在忽略注解
     */
    public boolean isIgnored(String statementId) throws ClassNotFoundException {
        var method = getMethod(statementId);;
        return method == null || method.getAnnotation(Ignore.class) != null;
    }
    /**
     * @return 判断是否为排除不过滤的语句
     */
    public boolean isExcludedStatementId(String statementId){
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeStatements();
        return CollUtil.isNotEmpty(excludeTables)
                && excludeTables.stream().anyMatch(statementId::equals);
    }
    /**
     * @return 判断是否为排除不过滤的表  只能解决单表查询
     */
    public boolean isExcludedTable(String statementId){
        String name = getTableName(statementId);
        List<String> excludeTables = evaConfig.getDataPermission().getExcludeTables();
        return CollUtil.isNotEmpty(excludeTables)
                && excludeTables.stream().anyMatch(name::equals);
    }

    /**
     * 生成权限限制sql
     * @param dataPermission
     * @return
     */
    public static String permissionSql(List<ThreadUser.GrantedRoles> dataPermission, String tableName){
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
            return "true";
        }
        permissionSql.delete(permissionSql.lastIndexOf(" or "), permissionSql.length()-1);
        permissionSql.append(" ) ");

        return permissionSql.toString();
    }

    /**
     * mapper接口类
     */
    private Class getClass(String statementId) throws ClassNotFoundException {
        String className = statementId.substring(0,statementId.lastIndexOf("."));
        return Class.forName(className);
    }

    /**
     * statement接口方法
     */
    private Method getMethod(String statementId) throws ClassNotFoundException {
        Class clazz = getClass(statementId);
        // 判断类注解
        if (null == clazz || clazz.getAnnotation(Ignore.class) != null){
            return null;
        }
        return ReflectUtil.getMethodByName(clazz, statementId.substring(statementId.lastIndexOf(".") + 1));
    }

    /**
     * @return 得到TableName的注解值
     */
    private String getTableName(String statementId) {
        try {
            var mapper = getClass(statementId);
            var mapperTypes  = mapper.getGenericInterfaces();
            var type =(ParameterizedType) mapperTypes[0];
            var entity = Class.forName(type.getActualTypeArguments()[0].getTypeName());
           return entity.getAnnotation(TableName.class).value();
        }catch (Exception e){
            return "";
        }
    }

    /**
     * @return statement接口上的DatapermissionTable注解值
     */
    private String getDatapermissionTableName(String statementId) {
        try {
            return getMethod(statementId).getAnnotation(DatapermissionTable.class).value();
        }catch (Exception ignored){
            return "";
        }
    }

}
