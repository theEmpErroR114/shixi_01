package com.examsystem.mapper;

import com.examsystem.entity.PaperQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaperQuestionMapper {

    int insert(PaperQuestion paperQuestion);

    int batchInsert(@Param("list") List<PaperQuestion> list);

    int deleteByPaperId(@Param("paperId") Long paperId);

    List<PaperQuestion> selectByPaperId(@Param("paperId") Long paperId);
}
