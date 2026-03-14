package org.pkaq.core.mvc.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.enums.FrozenEnumm;

import java.time.LocalDateTime;

/**
 * @author PKAQ
 */
@Data
public class StdVo implements Vo {
    private Long id;

    @Schema(description = "乐观锁")
    private int revision;

    /**
     * 是否冻结（0 - 未冻结，1 - 冻结， -1 - 不可编辑）
     **/
    private FrozenEnumm frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "创建人Id")
    private Long createId;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime utcCreate;

    @Schema(description = "修改人Id")
    private Long modifyId;

    @Schema(description = "修改人")
    private String modifyBy;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime utcModify;

    @Schema(description = "备注")
    private String remark;
}
