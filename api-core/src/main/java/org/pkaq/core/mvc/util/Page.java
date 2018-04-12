package org.pkaq.core.mvc.util;

import lombok.Data;
import org.pkaq.core.mvc.entity.Entity;

import java.util.List;

/**
 * 分页模型类
 * @author: S.PKAQ
 * @Datetime: 2018/4/12 7:25
 */
@Data
public class Page<T extends Entity> {
    private List<T> data;
    /** 总数 **/
    private long total;

    /** 每页显示条数，默认 10 **/
    private int size = 10;

    /** 总页数 **/
    private long pages;

    /** 当前页 **/
    private int current = 1;
}
