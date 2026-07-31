package com.examsystem.mapper;

import com.examsystem.entity.QuestionOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目选项 Mapper 接口
 * 定义对 t_question_option 表的数据库操作
 */
public interface QuestionOptionMapper {

    /**
     * 新增单条题目选项
     * @param questionOption 题目选项实体对象
     * @return 受影响的行数
     */
    int insert(QuestionOption questionOption);

    /**
     * 根据题目ID删除所有选项（编辑题目时先清除旧选项再重新插入）
     * @param questionId 题目ID
     * @return 受影响的行数
     */
    int deleteByQuestionId(@Param("questionId") Long questionId);

    /**
     * 批量新增题目选项（创建/更新题目时一次性插入所有选项）
     * @param list 题目选项实体列表
     * @return 受影响的行数
     */
    int batchInsert(@Param("list") List<QuestionOption> list);

    /**
     * 根据题目ID查询所有选项（按 optionLabel 排序，如 A、B、C、D）
     * @param questionId 题目ID
     * @return 题目选项列表
     */
    List<QuestionOption> selectByQuestionId(@Param("questionId") Long questionId);
}
