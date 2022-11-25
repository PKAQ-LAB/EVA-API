package io.nerv.core.mvc.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 用于接收数组参数的虚拟对象
 * Datetime: 2017-06-13 22:06
 *
 * @author S.PKAQ
 */
@Data
public class SingleArray<T> implements Serializable {
    private ArrayList<T> param;
    private String status;
}