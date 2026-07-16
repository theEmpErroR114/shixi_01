package com.examsystem.mapper;

import com.examsystem.entity.QuestionOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionOptionMapper {

    int insert(QuestionOption questionOption);

    int deleteByQuestionId(@Param("questionId") Long questionId);

    int batchInsert(@Param("list") List<QuestionOption> list);

    List<QuestionOption> selectByQuestionId(@Param("questionId") Long questionId);
}
