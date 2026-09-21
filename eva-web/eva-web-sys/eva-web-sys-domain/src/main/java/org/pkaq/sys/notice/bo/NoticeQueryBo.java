package org.pkaq.sys.notice.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

/** 通知查询参数。 @author PKAQ */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQueryBo extends PageBo {
    private String title;
    private String type;
}
