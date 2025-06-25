package org.pkaq.core.mvc.convert;


import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.core.mvc.vo.Vo;

import java.util.List;

/**
 * @author PKAQ
 */
public interface Convert<T> {

    default <V extends Vo> V entityToDetailVo(T entity) {
        throw new UnsupportedOperationException("This method should be implemented in the sub-interface only.");
    }

    default <B extends Bo> T boToEntity(B bo) {
        throw new UnsupportedOperationException("This method should be implemented in the sub-interface only.");
    }

    default <V extends Vo> V entityToListVo(T entity) {
        throw new UnsupportedOperationException("This method should be implemented in the sub-interface only.");
    }

    default List<? extends Vo> entityToListVo(List<T> entity) {
        throw new UnsupportedOperationException("This method should be implemented in the sub-interface only.");
    }
}
