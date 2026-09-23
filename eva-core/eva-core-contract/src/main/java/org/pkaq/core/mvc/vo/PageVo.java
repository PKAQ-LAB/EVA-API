package org.pkaq.core.mvc.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页对象
 *
 * @author PKAQ
 */
@Data
public class PageVo<T> implements Vo {
    private List<T> records = Collections.emptyList();
    /**
     * 总数
     */
    private long total = 0;
    /**
     * 每页显示条数，默认 30
     */
    private long size = 30;
    /**
     * 当前页
     */
    private long current = 1;
}
