package tech.yunyue.sys.blacklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.yunyue.core.mybatis.mvc.service.mybatis.StdService;
import tech.yunyue.sys.blacklist.entity.BlackListEntity;
import tech.yunyue.sys.blacklist.mapper.BlackListMapper;

/**
 * 黑名单管理service
 */
@Service
@RequiredArgsConstructor
public class BlackListService extends StdService<BlackListMapper, BlackListEntity> {

}
