package com.petadoption.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 资源不存在异常，用于宠物、用户或申请等目标记录无法按条件找到的场景。
 * 继承 RuntimeException，业务代码可以直接抛出而无须显式声明。
 */
// 当该异常未被更具体的异常处理器拦截时，让 Spring MVC 默认返回 HTTP 404。
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * 使用具体资源说明构造异常。
     *
     * @param message 描述哪个资源不存在的错误消息
     */
    public ResourceNotFoundException(String message) {
        // 将资源缺失原因交给父类，供 404 页面显示。
        super(message);
    }
}
