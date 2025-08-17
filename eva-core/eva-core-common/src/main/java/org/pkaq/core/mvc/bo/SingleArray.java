package org.pkaq.core.mvc.bo;

import lombok.Data;
import org.pkaq.core.enums.FrozenEnumm;

import java.io.Serializable;
import java.util.List;

/**
 * 用于接收数组参数的虚拟对象
 * @author S.PKAQ
 */
@Data
public class SingleArray<T> implements Serializable {
    private List<T> param;
    private FrozenEnumm status;
}
