package com.examsystem.service.impl;

import com.examsystem.dto.PracticeConfigRequest;
import com.examsystem.dto.PracticeResultVO;
import com.examsystem.entity.PracticeRecord;
import com.examsystem.entity.Question;
import com.examsystem.entity.QuestionOption;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.PracticeRecordMapper;
import com.examsystem.mapper.QuestionMapper;
import com.examsystem.mapper.QuestionOptionMapper;
import com.examsystem.mapper.StudentCourseMapper;
import com.examsystem.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 练习业务实现 — 管理学生自主练习的全流程：随机出题 → 提交答案 → 自动评分 → 查看历史。
 *
 * 与考试模式的区别：
 * - 练习不限制时间，一次一道题，学生可以随时退出
 * - 练习题目随机抽取，每次可能不同
 * - 练习支持按课程、题型、难度筛选
 *
 * 评分规则：
 * - 单选(1)、判断(3)：精确匹配（忽略大小写）
 * - 多选(2)：答案标签排序后比较（如"AC"与"CA"视为相同）
 * - 填空(4)、简答(5)：精确匹配，返回参考答案供学生自行对照
 */
@Service
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    /**
     * 根据配置随机生成练习题。默认抽取10道题。
     * 校验学生是否已选修指定课程。
     * 判断题（type=3）如果数据库中没有选项，自动生成"对(A)/错(B)"两个默认选项。
     * 返回时清空answer和analysis字段，防止前端泄露答案。
     *
     * @param studentId 学生ID
     * @param config    练习配置（课程ID、题目类型、难度、数量）
     * @return 随机抽取的题目列表（不含答案和解析）
     * @throws BusinessException 如果学生未选修该课程
     */
    @Override
    public List<Question> generatePractice(Long studentId, PracticeConfigRequest config) {
        // 校验学生是否选了该课程
        List<Long> enrolledCourseIds = studentCourseMapper.selectCourseIdsByStudentId(studentId);
        if (config.getCourseId() != null && !enrolledCourseIds.contains(config.getCourseId())) {
            throw new BusinessException("您没有选修该课程");
        }
        // 默认抽取10道题
        int count = config.getCount() != null ? config.getCount() : 10;
        // 根据课程、题型、难度随机抽取题目
        List<Question> questions = questionMapper.findRandom(
                config.getCourseId(), config.getQuestionType(),
                config.getDifficulty(), count);
        for (Question q : questions) {
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(q.getQuestionId());
            // 判断题(questionType=3)：如果数据库中没有选项，自动生成"对"/"错"两个默认选项
            if ((options == null || options.isEmpty()) && q.getQuestionType() != null && q.getQuestionType() == 3) {
                options = new ArrayList<>();
                QuestionOption optTrue = new QuestionOption();
                optTrue.setOptionLabel("A");
                optTrue.setOptionContent("对");
                options.add(optTrue);
                QuestionOption optFalse = new QuestionOption();
                optFalse.setOptionLabel("B");
                optFalse.setOptionContent("错");
                options.add(optFalse);
            }
            q.setOptions(options);
            // 清空答案和解析，防止泄题
            q.setAnswer(null);
            q.setAnalysis(null);
        }
        return questions;
    }

    /**
     * 提交单道练习题的答案并获取反馈。流程：
     * 1. 查询题目获取正确答案
     * 2. 调用checkAnswer自动评分
     * 3. 保存练习记录到t_practice_record
     * 4. 返回结果（含正误、正确答案、解析、选项列表）
     *
     * @param studentId     学生ID
     * @param questionId    题目ID
     * @param studentAnswer 学生答案（单选/判断为单个字母如"A"，多选为排序后的标签串如"AC"）
     * @return 练习结果（正误判定、正确答案、解析、选项列表）
     */
    @Override
    public PracticeResultVO submitAnswer(Long studentId, Long questionId, String studentAnswer) {
        Question question = questionMapper.selectById(questionId);
        // 自动评分
        boolean isCorrect = checkAnswer(studentAnswer, question.getAnswer(), question.getQuestionType());

        // 保存练习记录
        PracticeRecord record = new PracticeRecord();
        record.setStudentId(studentId);
        record.setQuestionId(questionId);
        record.setStudentAnswer(studentAnswer);
        record.setIsCorrect(isCorrect ? 1 : 0);
        practiceRecordMapper.insert(record);

        // 构建返回结果
        PracticeResultVO vo = new PracticeResultVO();
        vo.setQuestionId(questionId);
        vo.setContent(question.getContent());
        vo.setStudentAnswer(studentAnswer);
        vo.setCorrectAnswer(question.getAnswer());
        vo.setIsCorrect(isCorrect);
        vo.setAnalysis(question.getAnalysis());
        // 选择题和判断题附带选项列表，填空题和简答题无选项
        if (question.getQuestionType() != null && (question.getQuestionType() == 1 || question.getQuestionType() == 2 || question.getQuestionType() == 3)) {
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(questionId);
            vo.setOptions(options.stream().map(o -> {
                PracticeResultVO.OptionVO ov = new PracticeResultVO.OptionVO();
                ov.setOptionLabel(o.getOptionLabel());
                ov.setOptionContent(o.getOptionContent());
                return ov;
            }).collect(Collectors.toList()));
        }
        return vo;
    }

    /**
     * 分页查询练习历史。
     */
    @Override
    public List<PracticeRecord> getPracticeHistory(Long studentId, Long courseId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return practiceRecordMapper.selectByStudentId(studentId, courseId, offset, pageSize);
    }

    @Override
    public Long countPracticeHistory(Long studentId, Long courseId) {
        return practiceRecordMapper.countByStudentId(studentId, courseId);
    }

    /**
     * 自动评分核心逻辑。根据题型采用不同的比较策略：
     *
     * - 单选(1)、判断(3)：精确匹配，trim()后忽略大小写比较
     * - 多选(2)：将学生答案和正确答案的字符分别排序后再比较
     *   例如 "AC" → 排序后 "AC"，"CA" → 排序后 "AC"，判定为相同
     * - 填空(4)、简答(5)：精确匹配（trim+忽略大小写），返回参考答案供学生自行对照
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
            case 2: // 多选：排序后比较，确保 "AC" 和 "CA" 判定为相同
                return sortChars(studentAnswer.trim().toUpperCase())
                        .equals(sortChars(correctAnswer.trim().toUpperCase()));
            default: // 填空(4)、简答(5)：精确匹配（忽略大小写）
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
    }

    /**
     * 将字符串中的字符按Unicode码点排序后返回。
     * 用于多选答案比较，确保 "BAC" → "ABC"。
     */
    private String sortChars(String s) {
        return s.chars().sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
