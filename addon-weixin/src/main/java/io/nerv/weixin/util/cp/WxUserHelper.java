package io.nerv.weixin.util.cp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.WxCpUserService;
import me.chanjar.weixin.cp.bean.WxCpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 微信用户同步工具类
 */
@Slf4j
@Component
public class WxUserHelper {
    @Autowired
    private WxCpUserService wxCpUserService;

    @Autowired
    private WxCpService wxCpService;

    /**
     * 根据userid获取用户
     * @param userId
     * @return
     * @throws WxErrorException
     */
    public WxCpUser get(String userId) {
        try {
            return wxCpService.getUserService().getById(userId);
        } catch (WxErrorException e) {
            return null;
        }
    }
    /**
     * 获取指定部门下所有用户
     * @param departId
     * @return
     */
    public List<WxCpUser> getByDepart(Long departId) throws WxErrorException {
        return wxCpService.getUserService().listByDepartment(departId, true, 0);
    }
    /**
     * 删除微信用户
     * @param ids
     * @throws WxErrorException
     */
    public void del(String... ids) throws WxErrorException {
        wxCpUserService.delete(ids);
    }
}
