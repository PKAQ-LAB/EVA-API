package org.pkaq.sys.notice.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

import java.time.LocalDateTime;

/** 通知新增编辑参数。 @author PKAQ */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeAoeBo extends StdBo {
    @NotBlank
    private String title;
    private String content;
    @NotBlank
    private String type;
    private String avatar;
    private LocalDateTime datetime;
}
