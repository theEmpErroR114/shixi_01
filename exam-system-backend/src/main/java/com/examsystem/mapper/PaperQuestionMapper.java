package com.examsystem.mapper;

import com.examsystem.entity.PaperQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 试卷题目关联 Mapper 接口
 * 定义对 t_paper_question 表的数据库操作
 * 该表维护试卷与题目的多对多关系，每条记录还包含题目在试卷中的序号和分值
 */
public interface PaperQuestionMapper {

    /**
     * 新增单条试卷题目关联
     * @param paperQuestion 试卷题目关联实体
     * @return 受影响的行数
     */
    int insert(PaperQuestion paperQuestion);

    /**
     * 批量新增试卷题目关联（保存试卷组卷时使用）
     * @param list 试卷题目关联实体列表
     * @return 受影响的行数
     */
    int batchInsert(@Param("list") List<PaperQuestion> list);

    /**
     * 根据试卷ID删除所有题目关联（编辑试卷组卷时先清除旧的关联）
     * @param paperId 试卷ID
     * @return 受影响的行数
     */
    int deleteByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID查询所有题目关联（含题目序号和分值）
     * @param paperId 试卷ID
     * @return 试卷题目关联列表
     */
    List<PaperQuestion> selectByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据题目ID删除关联（删除题目时需先清理引用，防止外键约束错误）
     * @param questionId 题目ID
     * @return 受影响的行数
     */
    int deleteByQuestionId(@Param("questionId") Long questionId);
}
