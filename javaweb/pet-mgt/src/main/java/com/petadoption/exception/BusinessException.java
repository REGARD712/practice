package com.petadoption.exception;

/**
 * 通用业务异常，用于在业务规则不满足时中断当前操作并传递可读的错误信息。
 * 继承 RuntimeException，属于非受检异常，调用方无须在每个方法签名中显式声明抛出。
 */
public class BusinessException extends RuntimeException {
    /**
     * 仅使用错误消息构造业务异常。
     *
     * @param message 向调用方说明业务失败原因的消息
     */
    public BusinessException(String message) {
        // 将业务错误消息交给 RuntimeException，供异常处理器或上层逻辑读取。
        super(message);
    }

    /**
     * 同时携带错误消息和原始异常，便于保留完整异常链。
     *
     * @param message 面向调用方的业务错误说明
     * @param cause   触发当前业务异常的底层原因
     */
    public BusinessException(String message, Throwable cause) {
        // 同时传递消息与 cause，避免丢失底层异常的堆栈信息。
        super(message, cause);
    }
}
