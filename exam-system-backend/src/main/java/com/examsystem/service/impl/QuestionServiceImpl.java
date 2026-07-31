package com.examsystem.service.impl;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.entity.Question;
import com.examsystem.entity.QuestionOption;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.ExamAnswerMapper;
import com.examsystem.mapper.PaperQuestionMapper;
import com.examsystem.mapper.PracticeRecordMapper;
import com.examsystem.mapper.QuestionMapper;
import com.examsystem.mapper.QuestionOptionMapper;
import com.examsystem.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目业务实现 — 管理题目的增删改查。
 *
 * 题目类型（questionType）：
 * 1=单选、2=多选、3=判断、4=填空、5=简答
 *
 * 业务规则：
 * - 单选(1)和多选(2)至少需要2个选项，且选项内容不能为空
 * - 多选(2)至少需要2个正确选项
 * - 判断(3)、填空(4)、简答(5)可以没有选项（选项可为空）
 * - 删除题目时必须按顺序清理FK引用（详见deleteQuestion方法注释）
 *
 * 选项更新策略：采用"先删后增"（replace模式），而非逐一比对差异。
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    /**
     * 分页查询题目（无课程权限过滤，供管理员使用）。
     *
     * @param courseId     课程ID筛选（可为null）
     * @param questionType 题型：1-5，null=全部
     * @param difficulty   难度：1=易，2=中，3=难，null=全部
     * @param keyword      搜索关键词
     * @param page         页码（从1开始）
     * @param pageSize     每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<Question> listQuestions(Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Question> list = questionMapper.findByFilters(courseId, questionType, difficulty, keyword, offset, pageSize);
        Long total = questionMapper.countByFilters(courseId, questionType, difficulty, keyword);
        return PageResult.of(total, page, pageSize, list);
    }

    /**
     * 分页查询题目（带课程权限过滤，供教师使用）。
     * 教师只能查看自己所属课程的题目。
     *
     * @param courseId     课程ID筛选
     * @param questionType 题型
     * @param difficulty   难度
     * @param keyword      搜索关键词
     * @param page         页码（从1开始）
     * @param pageSize     每页条数
     * @param courseIds    教师有权限的课程ID列表
     * @return 分页结果
     */
    @Override
    public PageResult<Question> listQuestions(Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize, List<Long> courseIds) {
        int offset = (page - 1) * pageSize;
        List<Question> list = questionMapper.findByFiltersAndCourseIds(courseId, questionType, difficulty, keyword, offset, pageSize, courseIds);
        Long total = questionMapper.countByFiltersAndCourseIds(courseId, questionType, difficulty, keyword, courseIds);
        return PageResult.of(total, page, pageSize, list);
    }

    /**
     * 获取题目详情，同时加载选项列表。
     *
     * @param questionId 题目ID
     * @return 题目实体（options字段已填充），不存在则返回null
     */
    @Override
    public Question getQuestionDetail(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question != null) {
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(questionId);
            question.setOptions(options);
        }
        return question;
    }

    /**
     * 创建题目。业务流程：
     * 1. 插入题目基本信息（同时设置teacher_id作为展示字段）
     * 2. 对单选/多选进行选项校验（至少2个选项、内容非空、多选至少2个正确选项）
     * 3. 批量插入选项到t_question_option（先设置questionId外键）
     *
     * 注意：选项校验在题目插入之后进行，因为校验主要针对选项而非题目。
     * 如果选项校验失败，事务回滚，题目插入也会被撤销。
     *
     * @param dto       题目DTO（含题型、内容、答案、解析、难度、选项列表）
     * @param teacherId 创建者教师ID（仅作展示字段，无外键约束）
     * @throws BusinessException 如果选项校验不通过（选项不足、内容为空、多选正确选项不足2个）
     */
    @Override
    @Transactional
    public void createQuestion(QuestionDTO dto, Long teacherId) {
        // 第一步：插入题目基本信息
        Question question = new Question();
        question.setCourseId(dto.getCourseId());
        question.setQuestionType(dto.getQuestionType());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        question.setTeacherId(teacherId);
        question.setCreateTime(LocalDateTime.now());
        questionMapper.insert(question);

        // 第二步：对单选/多选进行选项校验
        // 单选/多选必须至少有两个选项，且选项内容不能为空
        if (dto.getQuestionType() == 1 || dto.getQuestionType() == 2) {
            if (dto.getOptions() == null || dto.getOptions().size() < 2) {
                throw new BusinessException("单选题和多选题至少需要两个选项");
            }
            // 每个选项的内容不能为空
            for (com.examsystem.entity.QuestionOption opt : dto.getOptions()) {
                if (opt.getOptionContent() == null || opt.getOptionContent().trim().isEmpty()) {
                    throw new BusinessException("选项内容不能为空");
                }
            }
            // 多选题必须至少有两个正确选项
            if (dto.getQuestionType() == 2) {
                long correctCount = dto.getOptions().stream()
                        .filter(opt -> opt.getIsCorrect() != null && opt.getIsCorrect() == 1)
                        .count();
                if (correctCount < 2) {
                    throw new BusinessException("多选题必须至少选择两个正确答案");
                }
            }
        }
        // 第三步：批量插入选项
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (QuestionOption option : dto.getOptions()) {
                option.setQuestionId(question.getQuestionId());
            }
            questionOptionMapper.batchInsert(dto.getOptions());
        }
    }

    /**
     * 更新题目。采用"先更新基本信息，再替换选项"的策略。
     * 流程：
     * 1. 对单选/多选进行选项校验（与创建时相同）
     * 2. 更新题目基本信息（标题、答案、解析、难度等）
     * 3. 删除旧选项 → 插入新选项（replace模式）
     *
     * 选项采用replace而非merge的原因：前端传过来的是完整选项列表，
     * 逐一比对差异逻辑复杂且容易出错，先删后增最简单可靠。
     *
     * @param questionId 题目ID
     * @param dto        题目DTO
     * @throws BusinessException 如果选项校验不通过
     */
    @Override
    @Transactional
    public void updateQuestion(Long questionId, QuestionDTO dto) {
        // 选项校验（与create相同的逻辑）
        // 单选/多选必须至少有两个选项，且选项内容不能为空
        if (dto.getQuestionType() == 1 || dto.getQuestionType() == 2) {
            if (dto.getOptions() == null || dto.getOptions().size() < 2) {
                throw new BusinessException("单选题和多选题至少需要两个选项");
            }
            for (com.examsystem.entity.QuestionOption opt : dto.getOptions()) {
                if (opt.getOptionContent() == null || opt.getOptionContent().trim().isEmpty()) {
                    throw new BusinessException("选项内容不能为空");
                }
            }
            // 多选题必须至少有两个正确选项
            if (dto.getQuestionType() == 2) {
                long correctCount = dto.getOptions().stream()
                        .filter(opt -> opt.getIsCorrect() != null && opt.getIsCorrect() == 1)
                        .count();
                if (correctCount < 2) {
                    throw new BusinessException("多选题必须至少选择两个正确答案");
                }
            }
        }
        // 更新题目基本信息
        Question question = new Question();
        question.setQuestionId(questionId);
        question.setCourseId(dto.getCourseId());
        question.setQuestionType(dto.getQuestionType());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        questionMapper.update(question);

        // 选项替换：先删除旧选项，再插入新选项
        questionOptionMapper.deleteByQuestionId(questionId);
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (QuestionOption option : dto.getOptions()) {
                option.setQuestionId(questionId);
            }
            questionOptionMapper.batchInsert(dto.getOptions());
        }
    }

    /**
     * 删除题目。必须严格按照以下顺序清理FK引用，否则MySQL会因外键约束拒绝删除。
     *
     * t_question 被以下4张表引用（外键约束）：
     * 1. t_paper_question（试卷-试题关联） — 先删
     * 2. t_practice_record（练习记录）      — 次删
     * 3. t_exam_answer（考试答案）          — 再次删
     * 4. t_question_option（题目选项）      — 然后删
     * 5. t_question（题目本身）             — 最后删
     *
     * 顺序不能颠倒，否则会触发MySQL外键约束错误。
     *
     * @param questionId 题目ID
     */
    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        // 删除该题目的关联数据，顺序不可颠倒：
        // 1) 试卷关联 2) 练习记录 3) 考试答案 4) 选项 5) 题目本身
        paperQuestionMapper.deleteByQuestionId(questionId);   // 删除试卷-试题关联
        practiceRecordMapper.deleteByQuestionId(questionId);   // 删除练习记录
        examAnswerMapper.deleteByQuestionId(questionId);       // 删除考试答案
        questionOptionMapper.deleteByQuestionId(questionId);   // 删除题目选项
        questionMapper.deleteById(questionId);                  // 删除题目本身
    }
}
