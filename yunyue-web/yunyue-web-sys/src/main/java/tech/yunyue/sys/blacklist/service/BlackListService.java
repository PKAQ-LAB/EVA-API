package tech.yunyue.sys.blacklist.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.yunyue.core.log.annotation.BizLog;
import tech.yunyue.core.log.base.BizLogEnum;
import tech.yunyue.core.mybatis.mvc.service.mybatis.StdService;
import tech.yunyue.sys.blacklist.cache.BlackListCacheHelper;
import tech.yunyue.sys.blacklist.entity.BlackListEntity;
import tech.yunyue.sys.blacklist.mapper.BlackListMapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 黑名单管理service
 */
@Service
@RequiredArgsConstructor
@Schema(description = "黑名单管理")
public class BlackListService extends StdService<BlackListMapper, BlackListEntity> {
    private final BlackListCacheHelper blackListCacheHelper;

    /**
     * 初始化黑名单数据缓存  启动项目则加载到缓存
     */
    @PostConstruct
    public void init() {
        var blackList = this.list(null).stream().map(BlackListEntity::getTarget).collect(Collectors.toList());
        blackListCacheHelper.cachePut(blackList);
    }

    @BizLog(operateType= BizLogEnum.CREATE_UPDATE,description = "保存黑名单列表[{0}]",args = {"param:0.id"})
    @Override
    public void merge(BlackListEntity entity) {
        //新增/修改缓存
        if (!StringUtils.hasText(entity.getId())) {
            blackListCacheHelper.add(entity.getTarget());
        } else {
            BlackListEntity oldEntity = this.getById(entity.getId());
            blackListCacheHelper.add(oldEntity.getTarget(),entity.getTarget());
        }

        super.merge(entity);
    }

    @BizLog(operateType= BizLogEnum.DELETE,description = "删除黑名单列表[{0}]",args = {"param:0"})
    @Override
    public void delete(ArrayList<String> param) {
        super.delete(param);
        //删除完成后重新加载缓存
        this.init();
    }
}
