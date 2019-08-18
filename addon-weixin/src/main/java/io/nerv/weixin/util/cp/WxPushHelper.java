package io.nerv.weixin.util.cp;

import io.nerv.config.WxCpConfiguration;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.WxCpMessage;
import me.chanjar.weixin.cp.bean.WxCpMessageSendResult;
import me.chanjar.weixin.cp.bean.WxCpOauth2UserInfo;
import me.chanjar.weixin.cp.bean.article.MpnewsArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送工具类
 */
@Slf4j
@Component
public class WxPushHelper {
    @Autowired
    private WxCpConfiguration wxCpConfiguration;

    /**
     * 根据code获取微信用户id
     * @param code
     * @return
     */
    public String getWeiXinUserId(String code, int appId) throws WxErrorException {
        log.debug("根据code获取微信用户id");
        WxCpOauth2UserInfo userInfo = WxCpConfiguration.getCpService(appId).getOauth2Service().getUserInfo(code);

        return userInfo.getUserId();
    }
    /**
     * 给用户发送消息
     * userID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
     * @param appId     应用ID
     * @param userId    用户ID
     * @param msg       发送的消息内容
     * @throws WxErrorException
     */
    public void sendMessageToUsers(int appId, String userId, String msg) throws WxErrorException {
        log.debug("微信消息发送开始");
        WxCpMessage wxCpMessage = WxCpMessage.TEXT()
                                              .agentId(appId)
                                              .toUser(userId)
                                              .content(msg)
                                              .build();
        WxCpConfiguration.getCpService(appId).messageSend(wxCpMessage);
        log.debug("微信消息发送结束");
    }

    /**
     * 给用户发送消息
     * userID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
     * @param appId     应用ID
     * @param userId    用户ID
     * @throws WxErrorException
     */
    public void sendCardMessageToUsers(int appId, String userId, WxCpMessage textCard) throws WxErrorException {
        log.debug("微信消息发送开始");
        textCard.setAgentId(appId);
        textCard.setToUser(userId);
        WxCpConfiguration.getCpService(appId).messageSend(textCard);
        log.debug("微信消息发送结束");
    }

    /**
     * 给用户发送消息
     * userID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
     * @param appId     应用ID
     * @param userId    用户ID
     * @throws WxErrorException
     */
    public void sendCardMessageToUsers(int appId, String userId, String title, String description, String url, String btnText) throws WxErrorException {
        log.debug("微信消息发送开始");
        WxCpMessage wxCpMessagee = WxCpMessage.TEXTCARD()
                .agentId(appId)
                .toUser(userId)
                .title(title)
                .description(description)
                .url(url)
                .btnTxt(btnText)
                .build();
        WxCpConfiguration.getCpService(appId).messageSend(wxCpMessagee);
        log.debug("微信消息发送结束");
    }

    /**
     * 向用户组发送卡片消息
     * @param appId
     * @param partsId
     * @param title
     * @param description
     * @param url
     * @param btnText
     * @throws WxErrorException
     * @return
     */
    public WxCpMessageSendResult sendCardMessageToParts(int appId, String partsId, String title, String description, String url, String btnText) throws WxErrorException {
        log.debug("微信消息发送开始");
        WxCpMessage wxCpMessagee = WxCpMessage.TEXTCARD()
                .agentId(appId)
                .toParty(partsId)
                .title(title)
                .description(description)
                .url(url)
                .btnTxt(btnText)
                .build();
        return WxCpConfiguration.getCpService(appId).messageSend(wxCpMessagee);
    }

    /**
     * 给标签组用户发送消息
     * TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
     * @param appId
     * @param tag
     * @param msg
     * @throws WxErrorException
     */
    public void sendMessageToTags(int appId, String tag, String msg) throws WxErrorException {
        log.debug("微信消息发送开始");
        WxCpMessage wxCpMessagee = WxCpMessage.TEXT()
                .agentId(appId)
                .toTag(tag)
                .content(msg)
                .build();
        WxCpConfiguration.getCpService(appId).messageSend(wxCpMessagee);
        log.debug("微信消息发送结束");
    }

    /**
     * 给部门用户发送消息
     * PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
     * @param appId
     * @param part
     * @param msg
     * @throws WxErrorException
     * @return
     */
    public WxCpMessageSendResult sendMessageToPart(int appId, String part, String msg) throws WxErrorException {
        WxCpMessage wxCpMessagee = WxCpMessage.TEXT()
                .agentId(appId)
                .toParty(part)
                .content(msg)
                .build();
        return WxCpConfiguration.getCpService(appId).messageSend(wxCpMessagee);
    }

    /**
     *  发送图文消息
     * @param appId
     * @param part
     * @param article
     * @return
     * @throws WxErrorException
     */
    public WxCpMessageSendResult sendMpNewsMessageToPart(int appId, String part, MpnewsArticle article) throws WxErrorException {
        WxCpMessage wxCpMessagee = WxCpMessage.MPNEWS()
                .agentId(appId)
                .toParty(part)
                .addArticle(article)
                .build();
        return WxCpConfiguration.getCpService(appId).messageSend(wxCpMessagee);
    }

}
