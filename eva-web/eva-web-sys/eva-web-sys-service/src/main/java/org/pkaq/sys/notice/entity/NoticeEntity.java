package org.pkaq.sys.notice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.time.LocalDateTime;

/** 通知实体。 @author PKAQ */
@Data
@Alias("noticeEntity")
@TableName("SYS_NOTICE")
@EqualsAndHashCode(callSuper = true)
public class NoticeEntity extends StdEntity {
    @TableField(exist = false)
    private Long tenantId;
    private String title;
    private String content;
    private String type;
    private String avatar;
    private LocalDateTime datetime;
}
