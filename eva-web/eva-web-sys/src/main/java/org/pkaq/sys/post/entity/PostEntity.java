package org.pkaq.sys.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

/**
 * 岗位信息;
 *
 * @author AOC
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Alias("sys_post")
@TableName("SYS_POST")
public class PostEntity extends StdEntity {
    /**
     * 编码
     */
    private String code;
    /**
     * 岗位
     */
    private String title;
    /**
     * 职级
     */
    private String level;
    /**
     * 上级岗位ID
     */
    private String parentId;
    /**
     * 岗位路径
     */
    private String pathId;
    /**
     * 状态
     */
    private String status;
    /**
     * 排序
     */
    private Integer sorts;
}
