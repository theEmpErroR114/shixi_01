package com.examsystem.service;

import com.examsystem.dto.ExamResultVO;
import com.examsystem.dto.ExamSubmitRequest;
import com.examsystem.entity.Paper;

import java.util.List;

public interface ExamService {
    List<Paper> getAvailableExams(Long studentId);
    List<Paper> getUpcomingExams(Long studentId);
    Long startExam(Long studentId, Long paperId);
    List<com.examsystem.entity.Question> getExamQuestions(Long examRecordId, Long studentId);
    ExamResultVO submitExam(Long examRecordId, Long studentId, ExamSubmitRequest request);
    ExamResultVO getExamResult(Long examRecordId, Long studentId);
    List<com.examsystem.entity.ExamRecord> getExamHistory(Long studentId, Integer page, Integer pageSize);
    Long countExamHistory(Long studentId);
}
