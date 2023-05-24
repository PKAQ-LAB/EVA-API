package io.nerv.core.mvc.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageBo<T> implements Serializable {

    private static final int PAGE_NO = 1;
    private static final int PAGE_SIZE = 10;

    // 请求页码
    private Integer pageNo = PAGE_NO;

    // 单页条数
    private Integer pageSize = PAGE_SIZE;

    // 请求参数
    private T param;
}
