package org.pkaq.sys.notice.ctrl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.notice.bo.NoticeAoeBo;
import org.pkaq.sys.notice.bo.NoticeQueryBo;
import org.pkaq.sys.notice.service.NoticeService;
import org.pkaq.sys.notice.vo.NoticeVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author PKAQ
 */
@RestController
@RequiredArgsConstructor
public class NoticeCtrl extends Ctrl {
    private final NoticeService noticeService;

    @GetMapping("/notices")
    public Response<List<NoticeVo>> notices() {
        return success(this.noticeService.listAvailable());
    }

    @GetMapping("/sys/notice/list")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了通知列表[{0}]", args = {"param:0"})
    public Response<Object> list(NoticeQueryBo queryBo) {
        return success(this.noticeService.list(queryBo));
    }

    @GetMapping("/sys/notice/get/{id}")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了通知[{0}]", args = {"param:0"})
    public Response<Object> get(@PathVariable("id") Long id) {
        return success(this.noticeService.get(id));
    }

    @PostMapping("/sys/notice/edit")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了通知[{0}]", args = {"param:0"})
    public Response<Object> edit(@RequestBody @Valid NoticeAoeBo bo) {
        this.noticeService.edit(bo);
        return success();
    }

    @PostMapping("/sys/notice/del")
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除了通知[{0}]", args = {"param:0"})
    public Response<Object> del(@RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.noticeService.delete(ids.getParam());
        return success();
    }

    @PostMapping("/sys/notice/switch")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "切换了通知状态[{0}]", args = {"param:0"})
    public Response<Object> switchFrozen(@RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.noticeService.switchFrozen(ids);
        return success();
    }
}
