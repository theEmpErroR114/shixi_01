package com.examsystem.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页结果 DTO
 * 所有分页查询接口的统一返回格式
 * @param <T> 分页数据的类型
 */
@Data
public class PageResult<T> {
    /** 总记录数 */
    private Long total;
    /** 当前页码 */
    private Integer page;
    /** 每页条数 */
    private Integer pageSize;
    /** 当前页的数据列表 */
    private List<T> list;

    /**
     * 构造分页结果的静态工厂方法
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param list     当前页数据列表
     * @param <T>      数据类型
     * @return PageResult 实例
     */
    public static <T> PageResult<T> of(Long total, Integer page, Integer pageSize, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setList(list);
        return result;
    }
}
