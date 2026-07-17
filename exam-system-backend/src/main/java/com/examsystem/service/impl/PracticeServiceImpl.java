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

    @Override
    public List<Question> generatePractice(Long studentId, PracticeConfigRequest config) {
        // 校验学生是否选了该课程
        List<Long> enrolledCourseIds = studentCourseMapper.selectCourseIdsByStudentId(studentId);
        if (config.getCourseId() != null && !enrolledCourseIds.contains(config.getCourseId())) {
            throw new BusinessException("您没有选修该课程");
        }
        int count = config.getCount() != null ? config.getCount() : 10;
        List<Question> questions = questionMapper.findRandom(
                config.getCourseId(), config.getQuestionType(),
                config.getDifficulty(), count);
        for (Question q : questions) {
            q.setOptions(questionOptionMapper.selectByQuestionId(q.getQuestionId()));
            q.setAnswer(null);
            q.setAnalysis(null);
        }
        return questions;
    }

    @Override
    public PracticeResultVO submitAnswer(Long studentId, Long questionId, String studentAnswer) {
        Question question = questionMapper.selectById(questionId);
        boolean isCorrect = checkAnswer(studentAnswer, question.getAnswer(), question.getQuestionType());

        PracticeRecord record = new PracticeRecord();
        record.setStudentId(studentId);
        record.setQuestionId(questionId);
        record.setStudentAnswer(studentAnswer);
        record.setIsCorrect(isCorrect ? 1 : 0);
        practiceRecordMapper.insert(record);

        PracticeResultVO vo = new PracticeResultVO();
        vo.setQuestionId(questionId);
        vo.setContent(question.getContent());
        vo.setStudentAnswer(studentAnswer);
        vo.setCorrectAnswer(question.getAnswer());
        vo.setIsCorrect(isCorrect);
        vo.setAnalysis(question.getAnalysis());
        if (question.getQuestionType() != null && question.getQuestionType() <= 2) {
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

    @Override
    public List<PracticeRecord> getPracticeHistory(Long studentId, Long courseId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return practiceRecordMapper.selectByStudentId(studentId, courseId, offset, pageSize);
    }

    @Override
    public Long countPracticeHistory(Long studentId, Long courseId) {
        return practiceRecordMapper.countByStudentId(studentId, courseId);
    }

    private boolean checkAnswer(String studentAnswer, String correctAnswer, Integer questionType) {
        if (studentAnswer == null || correctAnswer == null) return false;
        if (questionType == null) return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        switch (questionType) {
            case 1: // single choice
            case 3: // true/false
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case 2: // multi choice - sort both for comparison
                return sortChars(studentAnswer.trim().toUpperCase())
                        .equals(sortChars(correctAnswer.trim().toUpperCase()));
            default:
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
    }

    private String sortChars(String s) {
        return s.chars().sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
