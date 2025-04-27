package org.pkaq.core.mvc.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于接收数组参数的虚拟对象
 *
 * @author S.PKAQ
 */
@Data
public class DateRangeVo<T> implements Serializable {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date begin;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date end;

    private T obj;
}
