package com.examsystem.mapper;

import com.examsystem.entity.Paper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaperMapper {

    List<Paper> findByFilters(@Param("status") Integer status,
                              @Param("courseId") Long courseId, @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    Long countByFilters(@Param("status") Integer status,
                        @Param("courseId") Long courseId);

    List<Paper> findByFiltersAndCourseIds(@Param("status") Integer status,
                                          @Param("courseId") Long courseId, @Param("offset") Integer offset,
                                          @Param("limit") Integer limit, @Param("courseIds") List<Long> courseIds);

    Long countByFiltersAndCourseIds(@Param("status") Integer status,
                                     @Param("courseId") Long courseId, @Param("courseIds") List<Long> courseIds);

    Paper selectById(@Param("paperId") Long paperId);

    int insert(Paper paper);

    int update(Paper paper);

    int updateStatus(@Param("paperId") Long paperId, @Param("status") Integer status);

    int deleteById(@Param("paperId") Long paperId);

    List<Paper> findAvailableForStudent(@Param("courseIds") List<Long> courseIds,
                                        @Param("now") LocalDateTime now);

    List<Paper> findUpcomingForStudent(@Param("courseIds") List<Long> courseIds,
                                       @Param("now") LocalDateTime now);
}
