package com.examsystem.service;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.entity.Question;

public interface QuestionService {
    PageResult<Question> listQuestions(Long teacherId, Long courseId, Integer questionType, Integer difficulty, String keyword, Integer page, Integer pageSize);
    Question getQuestionDetail(Long questionId);
    void createQuestion(QuestionDTO dto, Long teacherId);
    void updateQuestion(Long questionId, QuestionDTO dto);
    void deleteQuestion(Long questionId);
}
