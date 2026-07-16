package com.examsystem.mapper;

import com.examsystem.entity.ExamAnswer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamAnswerMapper {

    int insert(ExamAnswer examAnswer);

    int batchInsert(@Param("list") List<ExamAnswer> list);

    List<ExamAnswer> selectByExamRecordId(@Param("examRecordId") Long examRecordId);
}
