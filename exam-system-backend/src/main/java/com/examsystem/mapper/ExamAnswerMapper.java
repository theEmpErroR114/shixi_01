package com.examsystem.mapper;

import com.examsystem.entity.ExamAnswer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试答案 Mapper 接口
 * 定义对 t_exam_answer 表的数据库操作
 */
public interface ExamAnswerMapper {

    /**
     * 新增单条考试答案记录
     * @param examAnswer 考试答案实体对象
     * @return 受影响的行数
     */
    int insert(ExamAnswer examAnswer);

    /**
     * 批量新增考试答案记录
     * @param list 考试答案实体列表
     * @return 受影响的行数
     */
    int batchInsert(@Param("list") List<ExamAnswer> list);

    /**
     * 根据考试记录ID查询所有答案
     * @param examRecordId 考试记录ID
     * @return 该次考试的所有答案列表
     */
    List<ExamAnswer> selectByExamRecordId(@Param("examRecordId") Long examRecordId);

    /**
     * 根据题目ID删除考试答案（删除题目时需先清理引用）
     * @param questionId 题目ID
     * @return 受影响的行数
     */
    int deleteByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据试卷ID删除考试答案（删除试卷时需先清理引用，避免外键约束错误）
     * 通过 t_exam_record 子查询找到该试卷下所有考试记录的答案
     * @param paperId 试卷ID
     * @return 受影响的行数
     */
    int deleteByPaperId(@Param("paperId") Long paperId);
}
