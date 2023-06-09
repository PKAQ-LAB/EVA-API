package tech.yunyue.sys.post.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author dmz
 */
@Schema(description = "岗位管理详情视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostDetailVo {
    /**
     * 记录id
     */
    @Schema(description = "记录id")
    private String id;
    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;
    /**
     * 岗位
     */
    @Schema(description = "岗位")
    private String title;
    /**
     * 职级
     */
    @Schema(description = "职级")
    private String level;
    /**
     * 上级岗位ID
     */
    @Schema(description = "上级岗位ID")
    private String parentId;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sorts;
    /**
     * 上级岗位名称
     */
    @Schema(description = "上级岗位名称")
    private String parentName;
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String modifyBy;
    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;
    /**
     * 乐观锁
     */
    private Integer revision;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
