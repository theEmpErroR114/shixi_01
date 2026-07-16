package com.examsystem.service;

import com.examsystem.dto.PracticeConfigRequest;
import com.examsystem.dto.PracticeResultVO;
import com.examsystem.entity.PracticeRecord;
import com.examsystem.entity.Question;

import java.util.List;

public interface PracticeService {
    List<Question> generatePractice(Long studentId, PracticeConfigRequest config);
    PracticeResultVO submitAnswer(Long studentId, Long questionId, String studentAnswer);
    List<PracticeRecord> getPracticeHistory(Long studentId, Long courseId, Integer page, Integer pageSize);
    Long countPracticeHistory(Long studentId, Long courseId);
}
