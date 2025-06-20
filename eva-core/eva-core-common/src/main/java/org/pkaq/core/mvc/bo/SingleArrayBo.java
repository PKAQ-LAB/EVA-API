package org.pkaq.core.mvc.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 用于接收数组参数的虚拟对象
 * @author S.PKAQ
 */
@Data
public class SingleArrayBo<T> implements Serializable {
    private ArrayList<T> param;
}