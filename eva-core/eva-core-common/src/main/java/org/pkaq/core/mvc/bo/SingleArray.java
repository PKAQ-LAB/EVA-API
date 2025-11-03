package org.pkaq.core.mvc.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 用于接收数组参数的虚拟对象
 * @author S.PKAQ
 */
@Data
public class SingleArray<T> implements Serializable {
    private Set<T> param;
}
