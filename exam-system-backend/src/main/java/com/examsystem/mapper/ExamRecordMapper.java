package com.examsystem.mapper;

import com.examsystem.entity.ExamRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试记录 Mapper 接口
 * 定义对 t_exam_record 表的数据库操作
 */
public interface ExamRecordMapper {

    /**
     * 新增考试记录（学生开始考试时创建）
     * @param examRecord 考试记录实体对象
     * @return 受影响的行数
     */
    int insert(ExamRecord examRecord);

    /**
     * 根据学生ID和试卷ID查询考试记录（用于检查是否已参加过该试卷的考试）
     * @param studentId 学生ID
     * @param paperId   试卷ID
     * @param status    考试状态（null 表示查所有状态）
     * @return 考试记录列表
     */
    List<ExamRecord> selectByStudentIdAndPaperId(@Param("studentId") Long studentId, @Param("paperId") Long paperId,
                                                 @Param("status") Integer status);

    /**
     * 分页查询学生的考试记录
     * @param studentId 学生ID
     * @param offset    偏移量
     * @param limit     每页条数
     * @return 考试记录列表
     */
    List<ExamRecord> selectByStudentIdWithPage(@Param("studentId") Long studentId, @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);

    /**
     * 统计学生的考试记录总数
     * @param studentId 学生ID
     * @return 考试记录总数
     */
    Long countByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据考试记录ID查询考试记录
     * @param examRecordId 考试记录ID
     * @return 考试记录实体，未找到返回 null
     */
    ExamRecord selectById(@Param("examRecordId") Long examRecordId);

    /**
     * 更新考试总分（自动批改后回写总分）
     * @param examRecordId 考试记录ID
     * @param totalScore   考试总分
     * @return 受影响的行数
     */
    int updateTotalScore(@Param("examRecordId") Long examRecordId, @Param("totalScore") BigDecimal totalScore);

    /**
     * 更新考试状态和提交时间（学生交卷时调用）
     * @param examRecordId 考试记录ID
     * @param status       新的考试状态（1=已提交）
     * @param submitTime   提交时间
     * @return 受影响的行数
     */
    int updateStatus(@Param("examRecordId") Long examRecordId, @Param("status") Integer status,
                     @Param("submitTime") LocalDateTime submitTime);

    /**
     * 查询学生的考试成绩列表（用于成绩趋势图，返回考试成绩的散列数据）
     * @param studentId 学生ID
     * @return 包含 paperId、totalScore、submitTime 等字段的 Map 列表
     */
    List<java.util.Map<String, Object>> selectExamScoresByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询某份试卷的所有学生成绩（用于成绩统计）
     * @param paperId 试卷ID
     * @return 包含 studentId、totalScore、submitTime 等字段的 Map 列表
     */
    List<java.util.Map<String, Object>> selectScoresByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID删除考试记录（删除试卷时需先清理引用，避免外键约束错误）
     * @param paperId 试卷ID
     * @return 受影响的行数
     */
    int deleteByPaperId(@Param("paperId") Long paperId);
}
