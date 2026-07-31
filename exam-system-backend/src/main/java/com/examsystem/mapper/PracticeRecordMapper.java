package com.examsystem.mapper;

import com.examsystem.entity.PracticeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 练习记录 Mapper 接口
 * 定义对 t_practice_record 表的数据库操作
 */
public interface PracticeRecordMapper {

    /**
     * 新增练习记录（学生确认答案后记录每次答题结果）
     * @param practiceRecord 练习记录实体对象
     * @return 受影响的行数
     */
    int insert(PracticeRecord practiceRecord);

    /**
     * 分页查询学生的练习记录
     * @param studentId 学生ID
     * @param courseId  课程ID（可选，为 null 时查询所有课程）
     * @param offset    偏移量
     * @param limit     每页条数
     * @return 练习记录列表
     */
    List<PracticeRecord> selectByStudentId(@Param("studentId") Long studentId, @Param("courseId") Long courseId,
                                           @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计学生的练习记录总数
     * @param studentId 学生ID
     * @param courseId  课程ID（可选）
     * @return 练习记录总数
     */
    Long countByStudentId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 根据题目ID删除练习记录（删除题目时需先清理引用，防止外键约束错误）
     * @param questionId 题目ID
     * @return 受影响的行数
     */
    int deleteByQuestionId(@Param("questionId") Long questionId);
}
