package tech.yunyue.core.log.events;

import tech.yunyue.core.log.base.LoginlogEntity;

public class LoginLogEvent extends LogEvent<LoginlogEntity>{
    public LoginLogEvent(LoginlogEntity entity) {
        super(entity);
    }
}
