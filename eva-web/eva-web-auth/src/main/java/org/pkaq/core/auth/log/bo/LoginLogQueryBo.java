package org.pkaq.core.auth.log.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

import java.time.LocalDateTime;

/**
 * 登录日志查询参数
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录日志查询参数")
public class LoginLogQueryBo extends PageBo {

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "是否登录成功")
    private Boolean success;

    @Schema(description = "开始时间")
    private LocalDateTime begin;

    @Schema(description = "结束时间")
    private LocalDateTime end;
}
