package com.examsystem.exception;

import com.examsystem.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 使用 Spring @RestControllerAdvice 捕获所有 Controller 抛出的异常，
 * 统一转换为 Result 格式返回给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常（BusinessException）
     * 使用异常中携带的 code 和 message 构造响应
     * @param e 业务异常
     * @return 包含错误码和错误信息的 Result
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理所有未被其他 Handler 捕获的异常
     * 记录完整堆栈到日志，返回通用 500 错误给前端，避免暴露内部细节
     * @param e 异常
     * @return code=500 的 Result
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error(500, "服务器内部错误");
    }
}
