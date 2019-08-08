package io.nerv.core.mvc.vo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页对象
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class PageVo<T> {
    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();

    /**
     * 总页数
     */
    private long total = 0;
    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;

    /**
     * 当前页
     */
    private long current = 1;
}
