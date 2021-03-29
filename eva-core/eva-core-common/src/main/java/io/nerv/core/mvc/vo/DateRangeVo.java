package io.nerv.core.mvc.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

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
    private Date begin;

    @ApiModelProperty("结束日期")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date end;

    @ApiModelProperty("参数对象")
    private T obj;
}