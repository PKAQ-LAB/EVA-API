package tech.yunyue.blacklist.ctrl;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.yunyue.core.mybatis.mvc.ctrl.mybatis.StdCtrl;
import tech.yunyue.blacklist.entity.BlackListEntity;
import tech.yunyue.blacklist.service.BlackListService;


/**
 * 黑名单管理控制器
 */
@Tag(name = "黑名单管理")
@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
public class BlackListCtrl extends StdCtrl<BlackListService,BlackListEntity> {
}
