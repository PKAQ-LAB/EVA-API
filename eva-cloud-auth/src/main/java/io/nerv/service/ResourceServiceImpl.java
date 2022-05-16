package io.nerv.service;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 资源与角色匹配关系管理业务类
 * <p>
 * 初始化的时候把资源与角色匹配关系缓存到Redis中，方便网关服务进行鉴权的时候获取
 */
@Service
public class ResourceServiceImpl {

    private Map<String, List<String>> resourceRolesMap;

    @PostConstruct
    public void initData() {
        //TODO 系统启动的时候执行一次
        // 这里需要通过数据库获取角色权限信息
        resourceRolesMap = new TreeMap<>();
        resourceRolesMap.put("/admin/hello", CollUtil.toList("ADMIN"));
        resourceRolesMap.put("/admin/user/currentUser", CollUtil.toList("ADMIN", "TEST"));
    }
}
