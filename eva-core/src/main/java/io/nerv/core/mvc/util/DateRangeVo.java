package io.nerv.core.mvc.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用于接收数组参数的虚拟对象
 * Datetime: 2017-06-13 22:06
 * @author S.PKAQ
 */
@Data
@ApiModel("带开始日期、 结束日期的对象接收器")
public class DateRangeVo<T> implements Serializable {
    @ApiModelProperty("开始日期")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate begin = LocalDate.now();

    @ApiModelProperty("结束日期")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate end = LocalDate.now().plusDays(7l);

    @ApiModelProperty("参数对象")
    private T obj;
}