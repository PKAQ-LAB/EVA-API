package io.nerv.core.exception;

/**
 * 关于反射的异常
 */
public class ReflectException extends RuntimeException{
    private String msg;

    public ReflectException(String msg){
        super();
        this.msg=msg;
    }

}
