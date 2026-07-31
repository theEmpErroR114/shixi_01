package com.examsystem.service;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.entity.Question;

import java.util.List;

/**
 * 题目业务接口 — 负责题目的CRUD（增删改查）。
 * 题目类型：1=单选、2=多选、3=判断、4=填空、5=简答。
 * 题目属于课程（course_id），由教师创建（teacher_id仅作展示字段）。
 */
public interface QuestionService {

    /**
     * 分页查询题目列表（无课程权限过滤，供管理员使用）。
     * 支持按课程、题型、难度、关键词筛选。
     *
     * @param courseId     课程ID（可为null）
     * @param questionType 题型：1-5，null=全部
     * @param difficulty   难度：1=易，2=中，3=难，null=全部
     * @param keyword      搜索关键词（题目内容模糊匹配）
     * @param page         页码（从1开始）
     * @param pageSize     每页条数
     * @return 分页结果
     */
    PageResult<Question> listQuestions(Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize);

    /**
     * 分页查询题目列表（带课程权限过滤，供教师使用）。
     * 教师只能查看自己所属课程的题目。
     *
     * @param courseId     课程ID
     * @param questionType 题型
     * @param difficulty   难度
     * @param keyword      关键词
     * @param page         页码
     * @param pageSize     每页条数
     * @param courseIds    教师有权限的课程ID列表
     * @return 分页结果
     */
    PageResult<Question> listQuestions(Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize, List<Long> courseIds);

    /**
     * 获取题目详情，含选项列表。
     *
     * @param questionId 题目ID
     * @return 题目实体（options字段已填充），不存在则返回null
     */
    Question getQuestionDetail(Long questionId);

    /**
     * 创建题目。同时保存题目基本信息和选项列表。
     * 校验规则：
     * - 单选题(1)和多选题(2)至少需要2个选项
     * - 选项内容不能为空
     * - 多选题(2)至少需要2个正确选项
     *
     * @param dto       题目DTO（含题型、内容、答案、解析、难度、选项列表）
     * @param teacherId 创建者教师ID（仅作展示）
     * @throws BusinessException 如果选项校验不通过
     */
    void createQuestion(QuestionDTO dto, Long teacherId);

    /**
     * 更新题目。先更新题目基本信息，再删除旧选项并插入新选项（replace模式）。
     * 校验规则与创建时相同。
     *
     * @param questionId 题目ID
     * @param dto        题目DTO
     * @throws BusinessException 如果选项校验不通过
     */
    void updateQuestion(Long questionId, QuestionDTO dto);

    /**
     * 删除题目。必须按顺序清理所有外键引用，否则MySQL会因FK约束拒绝删除。
     * 删除顺序：t_paper_question（试卷关联）→ t_practice_record（练习记录）
     * → t_exam_answer（考试答案）→ t_question_option（选项）→ t_question（题目本身）。
     *
     * @param questionId 题目ID
     */
    void deleteQuestion(Long questionId);
}
