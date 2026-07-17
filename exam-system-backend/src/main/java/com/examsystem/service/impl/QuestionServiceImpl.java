package com.examsystem.service.impl;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.entity.Question;
import com.examsystem.entity.QuestionOption;
import com.examsystem.mapper.QuestionMapper;
import com.examsystem.mapper.QuestionOptionMapper;
import com.examsystem.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Override
    public PageResult<Question> listQuestions(Long teacherId, Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Question> list = questionMapper.findByFilters(teacherId, courseId, questionType, difficulty, keyword, offset, pageSize);
        Long total = questionMapper.countByFilters(teacherId, courseId, questionType, difficulty, keyword);
        return PageResult.of(total, page, pageSize, list);
    }

    @Override
    public PageResult<Question> listQuestions(Long teacherId, Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize, List<Long> courseIds) {
        int offset = (page - 1) * pageSize;
        List<Question> list = questionMapper.findByFiltersAndCourseIds(teacherId, courseId, questionType, difficulty, keyword, offset, pageSize, courseIds);
        Long total = questionMapper.countByFiltersAndCourseIds(teacherId, courseId, questionType, difficulty, keyword, courseIds);
        return PageResult.of(total, page, pageSize, list);
    }

    @Override
    public Question getQuestionDetail(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question != null) {
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(questionId);
            question.setOptions(options);
        }
        return question;
    }

    @Override
    @Transactional
    public void createQuestion(QuestionDTO dto, Long teacherId) {
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

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (QuestionOption option : dto.getOptions()) {
                option.setQuestionId(question.getQuestionId());
            }
            questionOptionMapper.batchInsert(dto.getOptions());
        }
    }

    @Override
    @Transactional
    public void updateQuestion(Long questionId, QuestionDTO dto) {
        Question question = new Question();
        question.setQuestionId(questionId);
        question.setCourseId(dto.getCourseId());
        question.setQuestionType(dto.getQuestionType());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        questionMapper.update(question);

        questionOptionMapper.deleteByQuestionId(questionId);
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (QuestionOption option : dto.getOptions()) {
                option.setQuestionId(questionId);
            }
            questionOptionMapper.batchInsert(dto.getOptions());
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        questionOptionMapper.deleteByQuestionId(questionId);
        questionMapper.deleteById(questionId);
    }
}
