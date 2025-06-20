package org.pkaq.sys.tenant.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.Vo;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 租户管理详情视图对象
 *
 * @author PKAQ
 */
@Schema(description = "租户管理详情视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantDetailVo implements Vo {
    private String id;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户类型")
    private String type;

    @Schema(description = "全称")
    private String fullName;

    @Schema(description = "证件类型")
    private String cardType;

    @Schema(description = "证件号")
    private String idCard;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系方式")
    private String contactTel;

    @Schema(description = "授权用户数")
    private int authUserCount;

    @Schema(description = "到期时间")
    private Date expirationDate;

    @Schema(description = "租户管理员账号")
    private String adminAccount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "创建时间")
    private LocalDateTime gmtCreate;

    @Schema(description = "修改人")
    private String modifyBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "修改时间")
    private LocalDateTime gmtModify;
}
