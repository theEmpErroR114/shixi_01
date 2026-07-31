package com.examsystem.service.impl;

import com.examsystem.dto.ExamResultVO;
import com.examsystem.dto.ExamSubmitRequest;
import com.examsystem.dto.PracticeResultVO;
import com.examsystem.entity.*;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.*;
import com.examsystem.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试业务实现 — 管理学生考试全生命周期：查看考试 → 开始考试 → 获取试题 → 提交答卷 → 查看成绩。
 *
 * 评分规则（与练习模式一致）：
 * - 单选(1)、判断(3)：学生答案与正确答案精确匹配（忽略大小写）
 * - 多选(2)：将双方答案的字符排序后比较（如 "AC" 与 "CA" 视为相同）
 * - 填空(4)、简答(5)：精确匹配
 *
 * 关键状态：考试记录status：0=进行中，1=已完成
 */
@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    /**
     * 获取学生可参加的考试列表。
     * 筛选条件：
     * - 试卷状态=1（已发布）
     * - 学生已选修该试卷所属课程
     * - start_date为空或已到达，end_date为空或未过期
     * - 学生尚未完成该试卷（exam_record中不存在status=1的记录）
     */
    @Override
    public List<Paper> getAvailableExams(Long studentId) {
        // 查询学生已选修的课程ID列表
        List<Long> enrolledCourseIds = studentCourseMapper.selectCourseIdsByStudentId(studentId);
        if (enrolledCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 从数据库中筛选已发布且时间有效的试卷
        List<Paper> papers = paperMapper.findAvailableForStudent(enrolledCourseIds, LocalDateTime.now());
        // 进一步过滤掉学生已经完成过的试卷
        return papers.stream().filter(p -> {
            List<ExamRecord> records = examRecordMapper.selectByStudentIdAndPaperId(studentId, p.getPaperId(), 1);
            return records.isEmpty();
        }).collect(Collectors.toList());
    }

    /**
     * 获取即将开始的考试列表。
     * 筛选条件：试卷已发布、start_date不为空且尚未到达、学生已选修对应课程、学生未完成。
     */
    @Override
    public List<Paper> getUpcomingExams(Long studentId) {
        List<Long> enrolledCourseIds = studentCourseMapper.selectCourseIdsByStudentId(studentId);
        if (enrolledCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 从数据库筛选已发布但尚未开始的试卷
        List<Paper> papers = paperMapper.findUpcomingForStudent(enrolledCourseIds, LocalDateTime.now());
        // 过滤掉已完成的
        return papers.stream().filter(p -> {
            List<ExamRecord> records = examRecordMapper.selectByStudentIdAndPaperId(studentId, p.getPaperId(), 1);
            return records.isEmpty();
        }).collect(Collectors.toList());
    }

    /**
     * 开始考试。创建一条status=0（进行中）的考试记录。
     * 校验流程：
     * 1. 试卷必须存在且状态为已发布(1)
     * 2. 考试开始时间未到则拒绝
     * 3. 考试截止时间已过则拒绝
     * 4. 学生不能重复参加已完成的考试
     *
     * @param studentId 学生ID
     * @param paperId   试卷ID
     * @return 新创建的考试记录ID
     * @throws BusinessException 如果试卷不可作答、未开始、已截止或已完成
     */
    @Override
    public Long startExam(Long studentId, Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        // 验证试卷存在且已发布
        if (paper == null || paper.getStatus() != 1) {
            throw new BusinessException("该试卷不可作答");
        }
        // 验证开始时间：start_date不为空且尚未到达
        if (paper.getStartDate() != null && paper.getStartDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("该考试尚未开始");
        }
        // 验证截止时间：end_date不为空且已过期
        if (paper.getEndDate() != null && paper.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该考试已截止");
        }
        // 防止重复参加：检查是否已有已完成(status=1)的考试记录
        List<ExamRecord> completed = examRecordMapper.selectByStudentIdAndPaperId(studentId, paperId, 1);
        if (!completed.isEmpty()) {
            throw new BusinessException("你已完成该测验");
        }
        // 创建考试记录，初始状态=0（进行中）
        ExamRecord record = new ExamRecord();
        record.setStudentId(studentId);
        record.setPaperId(paperId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        examRecordMapper.insert(record);
        return record.getExamRecordId();
    }

    /**
     * 获取考试试题。每道题的answer和analysis字段会被清空以防泄题。
     * 判断题（type=3）如果数据库没有选项记录，自动生成"对/错"两个默认选项。
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID（用于权限校验）
     * @return 试题列表（不含答案和解析）
     * @throws BusinessException 如果无权访问或考试已提交
     */
    @Override
    public List<Question> getExamQuestions(Long examRecordId, Long studentId) {
        // 权限校验：考试记录必须属于该学生
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || !record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权访问该测验");
        }
        // 状态校验：已提交的考试不能再次获取试题
        if (record.getStatus() != 0) {
            throw new BusinessException("该测验已提交或已过期");
        }
        // 根据试卷获取试题列表
        List<PaperQuestion> pqList = paperQuestionMapper.selectByPaperId(record.getPaperId());
        List<Question> questions = new ArrayList<>();
        for (PaperQuestion pq : pqList) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(pq.getQuestionId());
            // 判断题自动生成默认选项：A-对, B-错
            if ((options == null || options.isEmpty()) && q.getQuestionType() != null && q.getQuestionType() == 3) {
                options = new ArrayList<>();
                QuestionOption optA = new QuestionOption();
                optA.setOptionLabel("A");
                optA.setOptionContent("对");
                optA.setIsCorrect(0);
                options.add(optA);
                QuestionOption optB = new QuestionOption();
                optB.setOptionLabel("B");
                optB.setOptionContent("错");
                optB.setIsCorrect(0);
                options.add(optB);
            }
            q.setOptions(options);
            // 清空答案和解析，防止泄题
            q.setAnswer(null);
            q.setAnalysis(null);
            questions.add(q);
        }
        return questions;
    }

    /**
     * 提交考试答卷。事务保证原子性。核心流程：
     * 1. 权限和状态校验
     * 2. 遍历每道题，自动评分
     * 3. 批量插入答案
     * 4. 更新考试记录的总分和状态
     * 5. 返回成绩结果

     * 评分规则：答对得满分（该题分值），答错得0分。
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID
     * @param request      包含各题答案的提交请求
     * @return 考试结果（总分、正确数、各题详情）
     * @throws BusinessException 如果无权访问或已重复提交
     */
    @Override
    @Transactional
    public ExamResultVO submitExam(Long examRecordId, Long studentId, ExamSubmitRequest request) {
        // 权限校验
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || !record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权访问该测验");
        }
        // 防止重复提交
        if (record.getStatus() != 0) {
            throw new BusinessException("该测验已提交，请勿重复提交");
        }

        List<PaperQuestion> pqList = paperQuestionMapper.selectByPaperId(record.getPaperId());

        BigDecimal totalScore = BigDecimal.ZERO;
        List<ExamAnswer> answers = new ArrayList<>();

        // 逐题评分
        for (PaperQuestion pq : pqList) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            // 从提交请求中匹配该题的答案
            ExamSubmitRequest.ExamAnswerItem answerItem = request.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(pq.getQuestionId()))
                    .findFirst().orElse(null);

            String studentAnswer = answerItem != null ? answerItem.getStudentAnswer() : "";
            // 调用checkAnswer进行自动评分
            boolean isCorrect = checkAnswer(studentAnswer, q.getAnswer(), q.getQuestionType());

            ExamAnswer answer = new ExamAnswer();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(pq.getQuestionId());
            answer.setStudentAnswer(studentAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            // 答对得该题满分，答错得0分
            BigDecimal score = isCorrect ? BigDecimal.valueOf(pq.getScore()) : BigDecimal.ZERO;
            answer.setScore(score);
            answers.add(answer);
            totalScore = totalScore.add(score);
        }

        // 批量保存答案、更新总分和状态
        examAnswerMapper.batchInsert(answers);
        examRecordMapper.updateTotalScore(examRecordId, totalScore);
        examRecordMapper.updateStatus(examRecordId, 1, LocalDateTime.now());

        return buildResult(examRecordId, studentId);
    }

    /**
     * 查看考试结果。只有已提交（status=1）的考试才能查看。
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID
     * @return 考试结果
     * @throws BusinessException 如果无权查看或未提交
     */
    @Override
    public ExamResultVO getExamResult(Long examRecordId, Long studentId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || !record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该成绩");
        }
        // 只有已提交的考试才能查看成绩
        if (record.getStatus() != 1) {
            throw new BusinessException("该测验尚未提交");
        }
        return buildResult(examRecordId, studentId);
    }

    /**
     * 分页查询考试历史。
     */
    @Override
    public List<ExamRecord> getExamHistory(Long studentId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return examRecordMapper.selectByStudentIdWithPage(studentId, offset, pageSize);
    }

    @Override
    public Long countExamHistory(Long studentId) {
        return examRecordMapper.countByStudentId(studentId);
    }

    /**
     * 构建考试结果VO。包含：
     * - 考试基本信息（试卷名、课程名、总分、试卷满分）
     * - 用时（提交时间 - 开始时间，单位分钟）
     * - 正确题目数、总题数
     * - 每道题的详细结果（学生答案、正确答案、得分、解析、选项列表）
     */
    private ExamResultVO buildResult(Long examRecordId, Long studentId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        List<ExamAnswer> answers = examAnswerMapper.selectByExamRecordId(examRecordId);

        ExamResultVO vo = new ExamResultVO();
        vo.setExamRecordId(examRecordId);
        vo.setPaperName(record.getPaperName());
        vo.setCourseName(record.getCourseName());
        vo.setTotalScore(record.getTotalScore());
        vo.setPaperTotalScore(record.getPaperTotalScore());
        // 计算考试用时（分钟）
        if (record.getStartTime() != null && record.getSubmitTime() != null) {
            long minutes = java.time.Duration.between(record.getStartTime(), record.getSubmitTime()).toMinutes();
            vo.setUsedMinutes((int) minutes);
        }

        // 统计正确题目数：过滤isCorrect == 1的答案
        long correctCount = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
        vo.setCorrectCount((int) correctCount);
        vo.setQuestionCount(answers.size());

        // 构建每道题的详细结果
        List<ExamResultVO.ExamAnswerDetail> details = new ArrayList<>();
        for (ExamAnswer answer : answers) {
            ExamResultVO.ExamAnswerDetail detail = new ExamResultVO.ExamAnswerDetail();
            detail.setQuestionId(answer.getQuestionId());
            detail.setContent(answer.getQuestionContent());
            detail.setQuestionType(answer.getQuestionType());
            detail.setStudentAnswer(answer.getStudentAnswer());
            detail.setCorrectAnswer(answer.getCorrectAnswer());
            detail.setAnalysis(answer.getAnalysis());
            detail.setIsCorrect(answer.getIsCorrect());
            detail.setScore(answer.getScore());
            // 加载该题的选项列表，用于前端展示
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(answer.getQuestionId());
            if (options != null && !options.isEmpty()) {
                detail.setOptions(options.stream().map(o -> {
                    PracticeResultVO.OptionVO ov = new PracticeResultVO.OptionVO();
                    ov.setOptionLabel(o.getOptionLabel());
                    ov.setOptionContent(o.getOptionContent());
                    return ov;
                }).collect(Collectors.toList()));
            }
            details.add(detail);
        }
        vo.setAnswers(details);
        return vo;
    }

    /**
     * 自动评分核心逻辑。根据题型采用不同的比较策略：
     *
     * - 单选(1)、判断(3)：精确匹配，trim()后忽略大小写比较
     * - 多选(2)：将学生答案和正确答案的字符分别排序后再比较，这样 "AC" 和 "CA" 被视为相同答案
     * - 填空(4)、简答(5)：精确匹配（trim+忽略大小写）
     * - questionType为null时退化到精确匹配
     *
     * @param studentAnswer  学生提交的答案
     * @param correctAnswer  数据库中的正确答案
     * @param questionType   题目类型（1-5）
     * @return true=正确，false=错误
     */
    private boolean checkAnswer(String studentAnswer, String correctAnswer, Integer questionType) {
        if (studentAnswer == null || correctAnswer == null) return false;
        if (questionType == null) return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        switch (questionType) {
            case 1: // 单选：精确匹配（忽略大小写）
            case 3: // 判断：精确匹配（忽略大小写）
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case 2: // 多选：排序后比较（忽略大小写）
                return sortChars(studentAnswer.trim().toUpperCase())
                        .equals(sortChars(correctAnswer.trim().toUpperCase()));
            default: // 填空(4)、简答(5)：精确匹配（忽略大小写）
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
    }

    /**
     * 将字符串中的字符排序后返回。用于多选答案比较。
     * 例如："BAC" → "ABC"，确保 "BAC" 和 "CBA" 比较时能判定为相同。
     */
    private String sortChars(String s) {
        return s.chars().sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
