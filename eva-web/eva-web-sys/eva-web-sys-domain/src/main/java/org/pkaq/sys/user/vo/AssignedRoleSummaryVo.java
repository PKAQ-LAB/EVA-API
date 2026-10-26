package org.pkaq.sys.user.vo;

import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

/**
 * 当前操作者可读取的已授角色摘要，不代表有效权限。
 * @author PKAQ
 * @date 2026-10-24
 */
@Data
public class AssignedRoleSummaryVo implements Vo {
    private Long id;
    private String name;
    private String code;
    private Integer frozen;
}
