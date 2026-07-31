package com.examsystem.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务逻辑中主动抛出异常，由 GlobalExceptionHandler 统一捕获并返回给前端
 * 例如：用户名或密码错误、课程下有关联题目无法删除等场景
 */
@Getter
public class BusinessException extends RuntimeException {
    /** 业务错误码（如 400、401、403 等） */
    private final Integer code;

    /**
     * 构造函数（指定错误码和错误信息）
     * @param code    业务错误码
     * @param message 错误提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数（默认错误码 400）
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}
