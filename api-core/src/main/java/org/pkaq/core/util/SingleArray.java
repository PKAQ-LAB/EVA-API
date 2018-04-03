package org.pkaq.core.util;

import lombok.Data;

import java.util.ArrayList;

/**
 * 用于接收数组参数的工具类
 * Datetime: 2017-06-13 22:06
 * @author S.PKAQ
 */
@Data
public class SingleArray<T> {
    private ArrayList<T> param;
}