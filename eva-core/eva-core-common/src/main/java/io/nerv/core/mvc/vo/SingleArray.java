package io.nerv.core.mvc.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 用于接收数组参数的虚拟对象
 * Datetime: 2017-06-13 22:06
 * @author S.PKAQ
 */
@Data
@ApiModel("list接收对象")
public class SingleArray<T> implements Serializable {
    @ApiModelProperty("对象数组")
    private ArrayList<T> param;
    @ApiModelProperty("状态字段")
    private String status;
}