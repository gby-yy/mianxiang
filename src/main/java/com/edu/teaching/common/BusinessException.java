package com.edu.teaching.common;

/**
 * 业务异常：用于需要直接向前端返回提示信息的场景，不会被全局异常处理器替换为“系统繁忙”
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
