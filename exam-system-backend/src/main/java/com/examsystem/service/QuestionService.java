package com.examsystem.service;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.entity.Question;

import java.util.List;

public interface QuestionService {
    PageResult<Question> listQuestions(Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize);
    PageResult<Question> listQuestions(Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize, List<Long> courseIds);
    Question getQuestionDetail(Long questionId);
    void createQuestion(QuestionDTO dto, Long teacherId);
    void updateQuestion(Long questionId, QuestionDTO dto);
    void deleteQuestion(Long questionId);
}
