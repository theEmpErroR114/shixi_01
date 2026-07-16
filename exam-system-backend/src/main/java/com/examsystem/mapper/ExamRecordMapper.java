package com.examsystem.mapper;

import com.examsystem.entity.ExamRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExamRecordMapper {

    int insert(ExamRecord examRecord);

    List<ExamRecord> selectByStudentIdAndPaperId(@Param("studentId") Long studentId, @Param("paperId") Long paperId,
                                                 @Param("status") Integer status);

    List<ExamRecord> selectByStudentIdWithPage(@Param("studentId") Long studentId, @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);

    Long countByStudentId(@Param("studentId") Long studentId);

    ExamRecord selectById(@Param("examRecordId") Long examRecordId);

    int updateTotalScore(@Param("examRecordId") Long examRecordId, @Param("totalScore") BigDecimal totalScore);

    int updateStatus(@Param("examRecordId") Long examRecordId, @Param("status") Integer status,
                     @Param("submitTime") LocalDateTime submitTime);

    List<java.util.Map<String, Object>> selectExamScoresByStudentId(@Param("studentId") Long studentId);

    List<java.util.Map<String, Object>> selectScoresByPaperId(@Param("paperId") Long paperId);
}
