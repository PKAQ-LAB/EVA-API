package org.pkaq.sys.notice.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

import java.time.LocalDateTime;

/** 通知视图对象。 @author PKAQ */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeVo extends StdVo {
    private String title;
    private String content;
    private String type;
    private String avatar;
    private LocalDateTime datetime;
}
