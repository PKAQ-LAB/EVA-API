/*
 * Copyright (c) 2011-2023, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.yunyue.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.sql.Connection;
import java.util.*;

/**
 * 添加全局入参到mybatis中 使用#{参数名}访问
 */
@SuppressWarnings({"rawtypes"})
@Intercepts(
    {
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "getBoundSql", args = {}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
    }
)
@Slf4j
public class GlobalParamsInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();
        BoundSql boundSql = null;
        if (target instanceof Executor) {
            final Executor executor = (Executor) target;
            Object parameter = args[1];
            addParameter(parameter);

            MappedStatement ms = (MappedStatement) args[0];
            if (args.length != 2 && ms.getSqlCommandType() == SqlCommandType.SELECT && args.length != 4) {
                // 几乎不可能走进这里面,除非使用Executor的代理对象调用query[args[6]]
                boundSql = (BoundSql) args[5];
            }
        } else {
            // StatementHandler
            final StatementHandler sh = (StatementHandler) target;
            boundSql = sh.getBoundSql();
        }
        addParameter(boundSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor || target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
    /**
     * boundSql中新增参数
     */
    protected void addParameter(BoundSql boundSql) {
        if (boundSql != null) {
            paramMap().entrySet().forEach(e-> boundSql.setAdditionalParameter(e.getKey(), e.getValue()));
        }
    }

    /**
     * 往parameterMap中增加参数
     */
    protected void addParameter(Object parameter) {
        try{
            ((Map)parameter).putAll(paramMap());
        }catch (Exception e){
            // 添加参数失败
            log.error("mybatis全局添加参数失败：" + e.getMessage());
        }
    }

    /**
     * @return 全局参数map
     */
    protected Map<String, Object> paramMap(){
        return new HashMap<>(){{
            put("tenant", ThreadUserHelper.getTenantId());
        }};
    }
}
