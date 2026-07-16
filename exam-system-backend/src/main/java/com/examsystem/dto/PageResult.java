package com.examsystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<T> list;

    public static <T> PageResult<T> of(Long total, Integer page, Integer pageSize, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setList(list);
        return result;
    }
}
