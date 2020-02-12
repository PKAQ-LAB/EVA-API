package io.nerv.core.exception;

import io.nerv.core.enums.BizCodeEnum;
import lombok.Getter;

/**
 * 图片上传异常
 */
@Getter
public class ImageUploadException extends RuntimeException{
    private String msg;

    public ImageUploadException(String msg) {
        super();
        this.msg = msg;
    }

    public ImageUploadException(BizCodeEnum bizCodeEnum) {
        super();
        this.msg = bizCodeEnum.getName();
    }
}