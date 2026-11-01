package org.pkaq.sys.dict.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.pkaq.core.mvc.vo.Response;
import java.util.List;

/**
 * 字典管理列表的当前请求维护能力，不改变通用响应。
 * @author PKAQ
 * @date 2026-10-31
 */
public final class DictListResponse extends Response<List<DictViewVo>> {
    @Getter
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final boolean businessWritable;

    private DictListResponse(boolean businessWritable) {
        this.businessWritable = businessWritable;
    }

    /** 保留原成功响应全部业务字段，能力位即使false也显式输出。 */
    public static DictListResponse fromSuccess(Response<List<DictViewVo>> original, boolean writable) {
        if (original == null || !original.isSuccess()) {
            throw new IllegalArgumentException("只允许成功字典响应附加维护能力");
        }
        DictListResponse response = new DictListResponse(writable);
        response.setSuccess(original.isSuccess());
        response.setCode(original.getCode());
        response.setMessage(original.getMessage());
        response.setMtype(original.getMtype());
        response.setArgs(original.getArgs());
        response.setData(original.getData());
        return response;
    }
}
