package com.examsystem.mapper;

import com.examsystem.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionMapper {

    List<Question> findByFilters(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId,
                                 @Param("questionType") Integer questionType, @Param("difficulty") Integer difficulty,
                                 @Param("keyword") String keyword, @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countByFilters(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId,
                        @Param("questionType") Integer questionType, @Param("difficulty") Integer difficulty,
                        @Param("keyword") String keyword);

    List<Question> findRandom(@Param("courseId") Long courseId, @Param("questionType") Integer questionType,
                              @Param("difficulty") Integer difficulty, @Param("count") Integer count);

    Question selectById(@Param("questionId") Long questionId);

    int insert(Question question);

    int update(Question question);

    int deleteById(@Param("questionId") Long questionId);
}
