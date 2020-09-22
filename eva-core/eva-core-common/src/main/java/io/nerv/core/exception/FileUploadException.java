package io.nerv.core.exception;

import io.nerv.core.enums.BizCodeEnum;
import lombok.Getter;

/**
 * 图片上传异常
 */
@Getter
public class FileUploadException extends RuntimeException{
    private String msg;

    public FileUploadException(String msg) {
        super();
        this.msg = msg;
    }

    public FileUploadException(BizCodeEnum bizCodeEnum) {
        super();
        this.msg = bizCodeEnum.getName();
    }
}