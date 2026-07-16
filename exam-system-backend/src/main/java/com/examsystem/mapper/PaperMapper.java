package com.examsystem.mapper;

import com.examsystem.entity.Paper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaperMapper {

    List<Paper> findByFilters(@Param("teacherId") Long teacherId, @Param("status") Integer status,
                              @Param("courseId") Long courseId, @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    Long countByFilters(@Param("teacherId") Long teacherId, @Param("status") Integer status,
                        @Param("courseId") Long courseId);

    Paper selectById(@Param("paperId") Long paperId);

    int insert(Paper paper);

    int update(Paper paper);

    int updateStatus(@Param("paperId") Long paperId, @Param("status") Integer status);

    int deleteById(@Param("paperId") Long paperId);
}
