package org.pkaq.core.mvc.vo;

import lombok.Data;
import org.pkaq.core.enums.FrozenEnumm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 用于接收数组参数的虚拟对象
 * @author S.PKAQ
 */
@Data
public class SingleArray<T> implements Serializable {
    private ArrayList<T> param;
    private FrozenEnumm status;
}
