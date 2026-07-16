package com.examsystem.mapper;

import com.examsystem.entity.PracticeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PracticeRecordMapper {

    int insert(PracticeRecord practiceRecord);

    List<PracticeRecord> selectByStudentId(@Param("studentId") Long studentId, @Param("courseId") Long courseId,
                                           @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countByStudentId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
