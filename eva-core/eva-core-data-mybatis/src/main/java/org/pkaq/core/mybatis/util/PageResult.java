package org.pkaq.core.mybatis.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import org.pkaq.core.mvc.vo.PageVo;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 自定义分页模型
 *
 * @author PKAQ
 */
@Setter
@Getter
public class PageResult<T> extends Page<T> {
    public PageResult(long current, long size) {
        super(current, size, 0L);
    }

    public <R> PageVo<R> map(Function<? super T, ? extends R> mapper) {
        PageVo<R> result = new PageVo<>();
        result.setRecords(this.getRecords().stream().map(mapper).collect(Collectors.toList()));
        result.setTotal(this.getTotal());
        result.setCurrent(this.getCurrent());
        result.setSize(this.getSize());

        return result;
    }
}
