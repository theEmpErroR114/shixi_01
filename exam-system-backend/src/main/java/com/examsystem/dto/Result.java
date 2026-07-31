package com.examsystem.dto;

import lombok.Data;

/**
 * 统一 API 响应结果 DTO
 * 所有接口统一使用此格式返回数据
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {
    /** 业务状态码，200 表示成功 */
    private Integer code;
    /** 提示信息 */
    private String message;
    /** 响应数据 */
    private T data;

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result 实例，code=200，message="success"
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     * @param <T> 数据类型
     * @return Result 实例，data=null
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败/错误响应
     * @param code    错误码（400/401/403/500 等）
     * @param message 错误提示信息
     * @param <T>     数据类型
     * @return Result 实例
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
