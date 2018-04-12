package org.pkaq.core.mvc.util;

import lombok.Data;

import java.util.ArrayList;

/**
 * 用于接收数组参数的虚拟对象
 * Datetime: 2017-06-13 22:06
 * @author S.PKAQ
 */
@Data
public class SingleArray<T> {
    /** 对象数组 **/
    private ArrayList<T> param;
    /** 状态字段 **/
    private String status;
}